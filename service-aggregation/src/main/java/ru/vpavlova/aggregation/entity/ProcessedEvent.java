package ru.vpavlova.aggregation.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@NoArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(onlyExplicitlyIncluded = true)
public class ProcessedEvent {

    @Id
    @EqualsAndHashCode.Include
    private String eventId;

    @ToString.Include
    private LocalDateTime processedAt = LocalDateTime.now();
}
