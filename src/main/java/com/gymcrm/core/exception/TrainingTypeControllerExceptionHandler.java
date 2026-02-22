package com.gymcrm.core.exception;

import com.gymcrm.controller.TrainingTypeController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice(assignableTypes = TrainingTypeController.class)
public class TrainingTypeControllerExceptionHandler {

    // --- Delegators (entry points) ---

    /** Handle Service/Mapper failures (e.g., entity to response mapping errors) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String method = request.getMethod();

        if (Objects.equals(method, "GET"))
            return handleGetDictionaryError(ex, request);
        else
            return handleGenericRuntimeError(ex, request);
    }

    /** Handle total system or persistence failures */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralFailure(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "System Metadata Error");
        body.put("message", "The training types catalog is currently unavailable.");
        body.put("details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // --- SRP SPECIFIC HANDLER METHODS ---

    /** GET: Robust error handling for Training Type retrieval */
    private ResponseEntity<Map<String, Object>> handleGetDictionaryError(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Dictionary Retrieval Failed");
        body.put("details", "An error occurred while fetching the list of training categories.");
        body.put("message", ex.getMessage());
        body.put("suggestion", "Verify database seed data for 'TrainingType' table.");

        // Return 404 if the logic implies "not found", otherwise 500
        HttpStatus status = ex.getMessage().toLowerCase().contains("not found")
                ? HttpStatus.NOT_FOUND
                : HttpStatus.INTERNAL_SERVER_ERROR;

        return ResponseEntity.status(status).body(body);
    }

    private ResponseEntity<Map<String, Object>> handleGenericRuntimeError(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Unexpected Error");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // --- UTILITIES ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("path", request.getRequestURI());
        body.put("error_type", errorType);
        body.put("module", "METADATA_SERVICE");
        return body;
    }
}