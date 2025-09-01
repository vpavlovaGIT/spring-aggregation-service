package ru.vpavlova.aggregation.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
public class ProcessedEvent {
    @Id
    private String eventId;

    private LocalDateTime processedAt = LocalDateTime.now();
}
