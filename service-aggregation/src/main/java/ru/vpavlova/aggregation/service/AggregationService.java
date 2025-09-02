package ru.vpavlova.aggregation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import ru.vpavlova.aggregation.config.ExternalServiceProperties;
import ru.vpavlova.aggregation.entity.OutboxEvent;
import ru.vpavlova.aggregation.repository.OutboxRepository;

import jakarta.annotation.PostConstruct;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceResponse;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class AggregationService {

    private final WebClient.Builder webClientBuilder;
    private final ExternalServiceProperties properties;
    private final Scheduler blockingScheduler;
    private final OutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    private CircuitBreaker firstServiceCircuitBreaker;
    private CircuitBreaker secondServiceCircuitBreaker;

    @PostConstruct
    public void init() {
        CircuitBreakerConfig firstServiceConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();
        firstServiceCircuitBreaker = circuitBreakerRegistry.circuitBreaker("firstService", firstServiceConfig);

        CircuitBreakerConfig secondServiceConfig = CircuitBreakerConfig.custom()
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .slidingWindowSize(10)
                .minimumNumberOfCalls(5)
                .build();
        secondServiceCircuitBreaker = circuitBreakerRegistry.circuitBreaker("secondService", secondServiceConfig);
    }

    public Mono<AggregatedServiceResponse> aggregateData(String param) {
        WebClient client = webClientBuilder.build();

        Mono<String> firstServiceResponse = client.get()
                .uri(properties.getFirstServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .transformDeferred(CircuitBreakerOperator.of(firstServiceCircuitBreaker))
                .subscribeOn(blockingScheduler)
                .onErrorResume(throwable -> {
                    return Mono.just("Fallback data from first service");
                });

        Mono<String> secondServiceResponse = client.get()
                .uri(properties.getSecondServiceUrl() + "?param=" + param)
                .retrieve()
                .bodyToMono(String.class)
                .transformDeferred(CircuitBreakerOperator.of(secondServiceCircuitBreaker))
                .subscribeOn(blockingScheduler)
                .onErrorResume(throwable -> {
                    return Mono.just("Fallback data from second service");
                });

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