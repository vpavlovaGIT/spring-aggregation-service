package ru.vpavlova.aggregation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Модель запроса с параметром")
public class Request {

    @Schema(description = "Параметр запроса, который будет передан сторонним сервисам", example = "123")
    private String param;
}
