package com.gymcrm.core.exception;

import com.gymcrm.controller.TrainingController;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice(assignableTypes = TrainingController.class)
public class TrainingControllerExceptionHandler {

    // --- Delegators (entry points) ---

    /** Handle @Valid failures (Empty fields, Blank strings) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = extractFieldErrors(ex);
        // TrainingController currently only has POST, so we delegate directly to robust POST logic
        return handlePostTrainingValidation(errors, request);
    }

    /** Handle Incorrect Data Types (e.g., sending "abc" for Duration/Date) or Malformed JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleSerializationError(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return handleIncorrectDataTypes(ex, request);
    }

    /** Handle Service Layer logic errors (e.g., Trainee/Trainer username not found) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        return handleTrainingBusinessLogicError(ex, request);
    }

    // --- Handler methods ---


    /** POST: Robust Validation for Training Creation */
    private ResponseEntity<Map<String, Object>> handlePostTrainingValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Training Creation Denied");
        body.put("details", "Mandatory session details are missing or empty.");
        body.put("validation_errors", errors);
        body.put("suggestion", "Ensure 'traineeUsername', 'trainerUsername', and 'trainingName' are provided.");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Specific Handler for Type Mismatches (String vs Integer/Date) */
    private ResponseEntity<Map<String, Object>> handleIncorrectDataTypes(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Invalid Data Format");
        body.put("details", "The request body contains incompatible data types.");
        body.put("message", "Check if 'trainingDuration' is an Integer and 'trainingDate' follows 'YYYY-MM-DD'.");
        body.put("technical_error", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /** GET/POST: Business Logic failures (e.g., Usernames not found) */
    private ResponseEntity<Map<String, Object>> handleTrainingBusinessLogicError(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Persistence Error");
        body.put("message", ex.getMessage());
        body.put("suggestion", "Verify that both the Trainee and Trainer usernames exist in the database.");
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // --- UTILITIES ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("path", request.getRequestURI());
        body.put("error_type", errorType);
        body.put("module", "TRAINING_SERVICE");
        return body;
    }

    private Map<String, String> extractFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return fieldErrors;
    }
}