package ru.vpavlova;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "logging.starter")
public class LoggingProperties {
    private boolean logWeb = true;

}
