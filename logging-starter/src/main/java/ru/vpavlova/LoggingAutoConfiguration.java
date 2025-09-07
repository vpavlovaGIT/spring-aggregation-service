package ru.vpavlova;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
@EnableConfigurationProperties(LoggingProperties.class)
public class LoggingAutoConfiguration {

    @Bean
    @ConditionalOnProperty(name = "logging.starter.log-web", havingValue = "true", matchIfMissing = true)
    public FilterRegistrationBean<LoggingServletFilter> loggingFilterRegistration() {
        FilterRegistrationBean<LoggingServletFilter> registration = new FilterRegistrationBean<>();
        registration.setFilter(new LoggingServletFilter());
        registration.addUrlPatterns("/*");
        registration.setOrder(1); 
        return registration;
    }
}
