package com.gymcrm.config.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Component
public class RestLoggingFilter implements Filter {
    private static final Logger logger = LoggerFactory.getLogger(RestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logDetails(requestWrapper, responseWrapper);
            // Important: Copy the cached body back to the original response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logDetails(ContentCachingRequestWrapper request, ContentCachingResponseWrapper response) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        String requestBody = new String(request.getContentAsByteArray());
        String responseBody = new String(response.getContentAsByteArray());

        logger.info("REST CALL - Method: [{}], URI: [{}], Status: [{}], Request: [{}], Response: [{}]",
                method, uri, status, requestBody, responseBody);
    }
}
