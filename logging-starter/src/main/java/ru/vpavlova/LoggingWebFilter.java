package ru.vpavlova;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

public class LoggingWebFilter implements WebFilter {

    private static final Logger log = LoggerFactory.getLogger(LoggingWebFilter.class);

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        log.info("Incoming: {} {}", exchange.getRequest().getMethod(), exchange.getRequest().getURI());

        return chain.filter(exchange)
                .doOnSuccess((done) ->
                        log.info("Outgoing: {}", exchange.getResponse().getStatusCode()));
    }
}


