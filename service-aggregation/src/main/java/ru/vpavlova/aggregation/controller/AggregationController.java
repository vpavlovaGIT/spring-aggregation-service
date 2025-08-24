package ru.vpavlova.aggregation.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.vpavlova.aggregation.service.AggregationService;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceRequest;
import ru.vpavlova.serviceaggregation.model.AggregatedServiceResponse;

@RestController
@RequestMapping("/api/aggregate")
@RequiredArgsConstructor
@Tag(name = "Aggregation API", description = "Контроллер агрегации данных из сторонних сервисов")
public class AggregationController {

    private final AggregationService aggregationService;

    @PostMapping
    @Operation(
            summary = "Агрегировать данные из двух внешних сервисов",
            requestBody = @RequestBody(
                    required = true,
                    description = "Объект с параметрами для запроса",
                    content = @Content(schema = @Schema(implementation = AggregatedServiceRequest.class))
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Успешный ответ с данными от двух сервисов",
                            content = @Content(schema = @Schema(implementation = AggregatedServiceResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Данные не найдены"
                    )
            }
    )
    public Mono<ResponseEntity<AggregatedServiceResponse>> aggregateData(@RequestBody AggregatedServiceRequest requestModel) {
        return aggregationService.aggregateData(requestModel.getParam())
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }
}
