package ru.vpavlova.aggregation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Агрегированный ответ от двух внешних сервисов")
public class Response {

    @Schema(description = "Ответ от первого сервиса", example = "value1")
    private String dataFromFirstService;

    @Schema(description = "Ответ от второго сервиса", example = "value2")
    private String dataFromSecondService;
}
