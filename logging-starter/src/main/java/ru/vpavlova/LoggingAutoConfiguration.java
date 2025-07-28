package ru.vpavlova;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "logging.starter.log-web", havingValue = "true")
    public FilterRegistrationBean<LoggingWebFilter> loggingFilterRegistration() {
        FilterRegistrationBean<LoggingWebFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingWebFilter());
        registration.addUrlPatterns("/*");
        return registration;
    }
}
