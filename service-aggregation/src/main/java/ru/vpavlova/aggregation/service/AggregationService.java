package ru.vpavlova.aggregation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public Mono<AggregatedServiceResponse> aggregateData(String param) {
        WebClient client = webClientBuilder.build();

        Mono<String> firstServiceResponse = client.get()
                .uri(properties.getFirstServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(blockingScheduler);

        Mono<String> secondServiceResponse = client.get()
                .uri(properties.getSecondServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(blockingScheduler);

        return Mono.zip(firstServiceResponse, secondServiceResponse)
                .map(tuple -> {
                    AggregatedServiceResponse response = new AggregatedServiceResponse();
                    response.setDataFromFirstService(tuple.getT1());
                    response.setDataFromSecondService(tuple.getT2());
                    return response;
                })
                .flatMap(response -> Mono.fromCallable(() -> {
                                    OutboxEvent outbox = new OutboxEvent();
                                    outbox.setPayload(objectMapper.writeValueAsString(response));
                                    return outboxRepository.save(outbox);
                                })
                                .subscribeOn(blockingScheduler)
                                .thenReturn(response)
                );
    }
}
