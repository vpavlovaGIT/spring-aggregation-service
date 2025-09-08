package ru.vpavlova.aggregation.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class IncomingEvent {
    private String id;
    private String param;
}
