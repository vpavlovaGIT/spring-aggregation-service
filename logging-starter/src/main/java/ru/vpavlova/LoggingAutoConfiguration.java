package ru.vpavlova;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.WebFilter;

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

    @Bean
    @ConditionalOnClass(RestTemplate.class)
    @ConditionalOnProperty(name = "logging.starter.log-web", havingValue = "true")
    public RestTemplateCustomizer loggingRestTemplateCustomizer() {
        return restTemplate -> restTemplate.getInterceptors()
                .add(new LoggingRestTemplateInterceptor());
    }

    @Bean
    @ConditionalOnClass(WebClient.class)
    @ConditionalOnProperty(name = "logging.starter.log-web", havingValue = "true")
    public WebClient.Builder webClientBuilderWithLogging() {
        return WebClient.builder()
                .filter(LoggingWebClientFilter.logRequest())
                .filter(LoggingWebClientFilter.logResponse());
    }

    @Bean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.REACTIVE)
    @ConditionalOnProperty(name = "logging.starter.log-web", havingValue = "true")
    public WebFilter loggingReactiveWebFilter() {
        return new ReactiveLoggingWebFilter();
    }

}
