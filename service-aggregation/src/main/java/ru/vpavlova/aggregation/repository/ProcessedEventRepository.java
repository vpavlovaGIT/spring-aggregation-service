package ru.vpavlova.aggregation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vpavlova.aggregation.entity.ProcessedEvent;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, String> {
}
