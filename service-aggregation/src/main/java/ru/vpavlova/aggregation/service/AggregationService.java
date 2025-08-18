package ru.vpavlova.aggregation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.vpavlova.aggregation.config.ExternalServiceProperties;
import ru.vpavlova.aggregation.dto.AggregatedServiceResponse;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final WebClient.Builder webClientBuilder;
    private final ExternalServiceProperties properties;
    private final Scheduler blockingScheduler; // внедряем кастомный Scheduler

    public Mono<AggregatedServiceResponse> aggregateData(String param) {
        WebClient client = webClientBuilder.build();

        Mono<String> firstServiceResponse = client.get()
                .uri(properties.getFirstServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(blockingScheduler); // используем кастомный Scheduler

        Mono<String> secondServiceResponse = client.get()
                .uri(properties.getSecondServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .subscribeOn(blockingScheduler);

        return Mono.zip(firstServiceResponse, secondServiceResponse)
                .map(tuple -> new AggregatedServiceResponse(tuple.getT1(), tuple.getT2()));
    }
}
