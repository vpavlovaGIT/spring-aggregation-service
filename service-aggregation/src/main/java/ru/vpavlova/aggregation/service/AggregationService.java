package ru.vpavlova.aggregation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.vpavlova.aggregation.config.ExternalServiceProperties;
import ru.vpavlova.aggregation.entity.OutboxEvent;
import ru.vpavlova.aggregation.repository.OutboxRepository;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceResponse;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final WebClient.Builder webClientBuilder;
    private final ExternalServiceProperties properties;
    private final Scheduler blockingScheduler;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @CircuitBreaker(name = "firstService", fallbackMethod = "firstServiceFallback")
    public Mono<String> callFirstService(String param) {
        return webClientBuilder.build()
                .get()
                .uri(properties.getFirstServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(blockingScheduler);
    }

    @CircuitBreaker(name = "secondService", fallbackMethod = "secondServiceFallback")
    public Mono<String> callSecondService(String param) {
        return webClientBuilder.build()
                .get()
                .uri(properties.getSecondServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(blockingScheduler);
    }

    // Fallback-методы (важно: сигнатура + Throwable)
    private Mono<String> firstServiceFallback(String param, Throwable ex) {
        return Mono.just("Default data from FIRST service for param " + param);
    }

    private Mono<String> secondServiceFallback(String param, Throwable ex) {
        return Mono.just("Default data from SECOND service for param " + param);
    }

    public Mono<AggregatedServiceResponse> aggregateData(String param) {
        Mono<String> first = callFirstService(param);
        Mono<String> second = callSecondService(param);

        return Mono.zip(first, second)
                .map(tuple -> {
                    AggregatedServiceResponse response = new AggregatedServiceResponse();
                    response.setDataFromFirstService(tuple.getT1());
                    response.setDataFromSecondService(tuple.getT2());
                    return response;
                })
                .flatMap(response -> Mono.fromCallable(() -> {
                                    OutboxEvent outbox = new OutboxEvent();
                                    try {
                                        outbox.setPayload(objectMapper.writeValueAsString(response));
                                    } catch (JsonProcessingException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return outboxRepository.save(outbox);
                                })
                                .subscribeOn(blockingScheduler)
                                .thenReturn(response)
                );
    }
}