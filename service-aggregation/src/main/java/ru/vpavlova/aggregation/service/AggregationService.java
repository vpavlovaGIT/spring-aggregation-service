package ru.vpavlova.aggregation.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.vpavlova.aggregation.config.ExternalServiceProperties;
import ru.vpavlova.aggregation.dto.Response;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final WebClient.Builder webClientBuilder;
    private final ExternalServiceProperties properties;

    public Mono<Response> aggregateData(String param) {
        Mono<String> firstServiceResponse = webClientBuilder.build()
                .get()
                .uri(properties.getFirstServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class);

        Mono<String> secondServiceResponse = webClientBuilder.build()
                .get()
                .uri(properties.getSecondServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class);

        return Mono.zip(firstServiceResponse, secondServiceResponse)
                .map(tuple -> new Response(tuple.getT1(), tuple.getT2()));
    }
}
