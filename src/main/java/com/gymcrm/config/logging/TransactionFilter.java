// Creates a UUID and pushes it to MDC. This ID is shared across all logs in the thread and sent back to the user via header

package com.gymcrm.config.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.core.annotation.Order;
import java.io.IOException;
import java.util.UUID;
/*
This filter generates a unique transactionId at the start of the request,
places it in the SLF4J context, and attaches it to the response header so it can be tracked.
*/
@Component
@Order(1) // Execute first to ensure ID is available for all subsequent logs
public class TransactionFilter implements Filter {

    private static final String TRANSACTION_ID_KEY = "transactionId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Generate or extract transaction ID
        String transactionId = UUID.randomUUID().toString();

        // Put in MDC (Mapped Diagnostic Context) for SLF4J to use in every log line
        MDC.put(TRANSACTION_ID_KEY, transactionId);

        // 2. Add to response header for downstream tracking
        if (response instanceof HttpServletResponse httpResponse) {
            httpResponse.setHeader("X-Transaction-ID", transactionId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            // Must clear MDC at the end of the thread's execution
            MDC.remove(TRANSACTION_ID_KEY);
        }
    }
}
