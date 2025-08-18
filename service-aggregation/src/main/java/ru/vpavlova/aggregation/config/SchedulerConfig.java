package ru.vpavlova.aggregation.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Configuration
public class SchedulerConfig {

    @Bean
    public Scheduler blockingScheduler() {
        return Schedulers.newBoundedElastic(50, Integer.MAX_VALUE, "blocking-pool");
    }
}
