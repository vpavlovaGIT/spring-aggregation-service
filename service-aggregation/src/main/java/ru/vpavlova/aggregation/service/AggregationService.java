package ru.vpavlova.aggregation.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
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

    @CircuitBreaker(name = "firstService", fallbackMethod = "firstServiceFallback")
    public String callFirstService(String param) {
        var client = restTemplateBuilder.build();
        return client.getForObject(properties.getFirstServiceUrl() + "?param=" + param, String.class);
    }

    @CircuitBreaker(name = "secondService", fallbackMethod = "secondServiceFallback")
    public String callSecondService(String param) {
        var client = restTemplateBuilder.build();
        return client.getForObject(properties.getSecondServiceUrl() + "?param=" + param, String.class);
    }

    private String firstServiceFallback(String param, Throwable ex) {
        return "Default data from FIRST service for param " + param;
    }

    private String secondServiceFallback(String param, Throwable ex) {
        return "Default data from SECOND service for param " + param;
    }

    public AggregatedServiceResponse aggregateData(String param) {
        var firstServiceResponse = callFirstService(param);
        var secondServiceResponse = callSecondService(param);

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
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Ошибка при сохранении события в outbox", e);
        }

        return response;
    }
}
