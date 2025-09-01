package ru.vpavlova.aggregation.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import ru.vpavlova.aggregation.entity.ProcessedEvent;
import ru.vpavlova.aggregation.repository.ProcessedEventRepository;
import ru.vpavlova.aggregation.service.AggregationService;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceResponse;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class KafkaConsumerIdempotent {
    private final ProcessedEventRepository processedMessageRepository;
    private final AggregationService aggregationService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.consumer-topic}", groupId = "${kafka.group-id}")
    public void consume(String payload) throws JsonProcessingException {
        Map<String, String> messageMap = objectMapper.readValue(payload, new TypeReference<>() {
        });
        String id = messageMap.get("id");
        String param = messageMap.get("param");

        if (processedMessageRepository.existsById(id)) {
            return;
        }

        AggregatedServiceResponse response = aggregationService.aggregateData(param).block();
        System.out.println("Aggregated response for param " + param + ": " + response);

        ProcessedEvent processed = new ProcessedEvent();
        processed.setEventId(id);
        processedMessageRepository.save(processed);
    }
}
