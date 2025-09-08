package ru.vpavlova.aggregation.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Service;
import ru.vpavlova.aggregation.dto.IncomingEvent;
import ru.vpavlova.aggregation.entity.ProcessedEvent;
import ru.vpavlova.aggregation.repository.ProcessedEventRepository;
import ru.vpavlova.aggregation.service.AggregationService;

@Service
@RequiredArgsConstructor
public class KafkaConsumerIdempotent {

    private final ProcessedEventRepository processedEventRepository;
    private final AggregationService aggregationService;

    @KafkaListener(
            topics = "${kafka.consumer-topic}",
            groupId = "${kafka.group-id}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(IncomingEvent event, Acknowledgment ack) {
        try {
            if (processedEventRepository.existsById(event.getId())) {
                ack.acknowledge();
                return;
            }

            var response = aggregationService.aggregateData(event.getParam());
            if (response != null) {
                System.out.println("Aggregated response for param " + event.getParam() + ": " + response);
            }

            var processed = new ProcessedEvent();
            processed.setEventId(event.getId());
            processedEventRepository.save(processed);

            ack.acknowledge();
        } catch (Exception e) {
            System.err.println("Ошибка при обработке события: " + e.getMessage());
        }
    }
}
