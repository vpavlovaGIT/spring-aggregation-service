package ru.vpavlova;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "logging.starter")
public class LoggingProperties {
    private boolean logWeb = true;

    public boolean isLogWeb() {
        return logWeb;
    }

    public void setLogWeb(boolean logWeb) {
        this.logWeb = logWeb;
    }
}
