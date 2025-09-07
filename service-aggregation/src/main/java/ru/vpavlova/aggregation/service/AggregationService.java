package ru.vpavlova.aggregation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.stereotype.Service;
import ru.vpavlova.aggregation.config.ExternalServiceProperties;
import ru.vpavlova.aggregation.entity.OutboxEvent;
import ru.vpavlova.aggregation.repository.OutboxRepository;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceResponse;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final RestTemplateBuilder restTemplateBuilder;
    private final ExternalServiceProperties properties;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    public AggregatedServiceResponse aggregateData(String param) {
        var client = restTemplateBuilder.build();

        var firstServiceResponse = client.getForObject(
                properties.getFirstServiceUrl() + "?param=" + param, String.class);

        var secondServiceResponse = client.getForObject(
                properties.getSecondServiceUrl() + "?param=" + param, String.class);

        if (firstServiceResponse == null && secondServiceResponse == null) {
            return null;
        }

        var response = new AggregatedServiceResponse();
        response.setDataFromFirstService(firstServiceResponse);
        response.setDataFromSecondService(secondServiceResponse);

        try {
            var outbox = new OutboxEvent();
            outbox.setPayload(objectMapper.writeValueAsString(response));
            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка при сохранении события в outbox", e);
        }

        return response;
    }
}
