package ru.vpavlova;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class LoggingServletFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(LoggingServletFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest req && response instanceof HttpServletResponse res) {
            log.info("Incoming: {} {}", req.getMethod(), req.getRequestURI());

            chain.doFilter(request, response);

            log.info("Outgoing: {} {}", res.getStatus(), req.getRequestURI());
        } else {
            chain.doFilter(request, response);
        }
    }
}
