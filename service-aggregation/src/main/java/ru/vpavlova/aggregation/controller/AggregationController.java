package ru.vpavlova.aggregation.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.tags.Tag;

import reactor.core.publisher.Mono;
import ru.vpavlova.aggregation.service.AggregationService;
import ru.vpavlova.serviceaggregation.api.ApiApi; // 👈 интерфейс из target/generated-sources
import ru.vpavlova.serviceaggregation.model.AggregatedServiceRequest;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceResponse;

import org.springframework.web.server.ServerWebExchange;

@RestController
@RequiredArgsConstructor
@Tag(name = "Aggregation API", description = "Контроллер агрегации данных из сторонних сервисов")
public class AggregationController implements ApiApi {

    private final AggregationService aggregationService;

    @Override
    public Mono<ResponseEntity<AggregatedServiceResponse>> apiAggregatePost(
            Mono<AggregatedServiceRequest> aggregatedServiceRequest,
            ServerWebExchange exchange) {

        return aggregatedServiceRequest
                .flatMap(req -> aggregationService.aggregateData(req.getParam()))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}


