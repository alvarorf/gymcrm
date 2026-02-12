package com.gymcrm.core.exception;

import com.gymcrm.controller.TrainerController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = TrainerController.class)
public class TrainerControllerExceptionHandler {

    // --- Delegators (entry points) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String method = request.getMethod();
        Map<String, String> errors = extractFieldErrors(ex);

        return switch (method) {
            case "POST" -> handleTrainerRegistrationValidation(errors, request);
            case "PUT" -> handleTrainerUpdateValidation(errors, request);
            case "PATCH" -> handleTrainerActivationValidation(errors, request);
            default -> handleGenericValidation(errors, request);
        };
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();

        // Handle the specific training list path vs. general profile path
        if (uri.contains("/trainings")) {
            return handleTrainingSearchError(ex, request);
        }

        return switch (method) {
            case "GET" -> handleTrainerProfileNotFound(ex, request);
            default -> handleGenericRuntimeError(ex, request);
        };
    }

    // --- Handler methods ---


    /** POST: Robust Validation for Trainer Registration */
    private ResponseEntity<Map<String, Object>> handleTrainerRegistrationValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Trainer Registration Denied");
        body.put("details", "Specialization (Training Type) is mandatory for Trainers.");
        body.put("validation_errors", errors);
        body.put("suggestion", "Ensure 'specialization' object contains a valid 'trainingTypeName'.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** GET: Handling trainer search/criteria failures */
    private ResponseEntity<Map<String, Object>> handleTrainingSearchError(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Training List Error");
        body.put("message", "Could not retrieve trainings: " + ex.getMessage());
        body.put("suggestion", "Verify that the trainer username and date range are correct.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** PUT: Validation for Profile Updates */
    private ResponseEntity<Map<String, Object>> handleTrainerUpdateValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Update Error");
        body.put("details", "Trainer updates require a valid username and active status.");
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** PATCH: Validation for Activation Toggle */
    private ResponseEntity<Map<String, Object>> handleTrainerActivationValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Activation Failure");
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** GET: Profile retrieval failures */
    private ResponseEntity<Map<String, Object>> handleTrainerProfileNotFound(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Trainer Not Found");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // --- UTILITIES ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("path", request.getRequestURI());
        body.put("error_type", errorType);
        body.put("module", "TRAINER_SERVICE");
        return body;
    }

    private Map<String, String> extractFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return fieldErrors;
    }

    private ResponseEntity<Map<String, Object>> handleGenericValidation(Map<String, String> errors, HttpServletRequest request) {
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }

    private ResponseEntity<Map<String, Object>> handleGenericRuntimeError(RuntimeException ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", ex.getMessage()));
    }
}