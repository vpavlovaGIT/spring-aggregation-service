package ru.vpavlova.aggregation.scheduler;

import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.vpavlova.aggregation.entity.OutboxEvent;
import ru.vpavlova.aggregation.repository.OutboxRepository;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxScheduler {

    private final OutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final MeterRegistry meterRegistry;

    @Value("${outbox.topic}")
    private String topic;

    @Value("${outbox.batch-size:100}")
    private int batchSize;

    @Value("${outbox.thread-pool:10}")
    private int threadPool;

    private ExecutorService executor;

    @PostConstruct
    public void initExecutor() {
        this.executor = Executors.newFixedThreadPool(threadPool);
        log.info("OutboxScheduler executor initialized with {} threads", threadPool);
    }

    @PreDestroy
    public void shutdownExecutor() {
        log.info("Shutting down OutboxScheduler executor...");
        if (executor != null) {
            executor.shutdown();
        }
    }

    @Scheduled(fixedDelayString = "10000")
    @Transactional
    public void publishOutboxEvents() {
        List<OutboxEvent> events = repository.findAndLockUnsent(batchSize);
        if (events.isEmpty()) {
            return;
        }

        log.info("Found {} unsent outbox events, processing...", events.size());

        List<CompletableFuture<Void>> futures = events.stream()
                .map(event -> CompletableFuture.runAsync(() -> {
                    try {
                        kafkaTemplate.send(topic, String.valueOf(event.getId()), event.getPayload()).get();
                        event.setSent(true);
                        meterRegistry.counter("outbox_publish_success").increment();
                        log.debug("Successfully sent event id={} to topic={}", event.getId(), topic);
                    } catch (Exception e) {
                        meterRegistry.counter("outbox_publish_failure").increment();
                        log.error("Failed to send event id={} payload={}", event.getId(), event.getPayload(), e);
                    }
                }, executor))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        repository.saveAll(events);
    }
}

