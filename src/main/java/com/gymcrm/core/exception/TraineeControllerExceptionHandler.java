package com.gymcrm.core.exception;

import com.gymcrm.controller.TraineeController;
import com.gymcrm.core.util.Nomenclature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice(assignableTypes = TraineeController.class)
public class TraineeControllerExceptionHandler {

    // --- Entry points (delegators) ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        // Ensure the path is exactly what the test expects
        Map<String, Object> body = new LinkedHashMap<>(); // Use LinkedHashMap for predictable order
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", request.getRequestURI());
        body.put("error_type", Nomenclature.ERR.TYPE_REG_FAILED);
        body.put("module", Nomenclature.ERR.MODULE_TRAINEE);

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage())
        );
        body.put("validation_errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String method = request.getMethod();
        String path = request.getRequestURI();

        // Determine error type based on Method and Path
        String errorType = Nomenclature.ERR.TYPE_INTERNAL;
        HttpStatus status = HttpStatus.NOT_FOUND;

        if ("GET".equalsIgnoreCase(method)) {
            errorType = Nomenclature.ERR.TYPE_NOT_FOUND;
        } else if ("DELETE".equalsIgnoreCase(method)) {
            errorType = Nomenclature.ERR.TYPE_DEL_FAILED;
        } else if ("PATCH".equalsIgnoreCase(method) && path.contains("/activation")) {
            errorType = Nomenclature.ERR.TYPE_TOGGLE_FAILED;
            status = HttpStatus.BAD_REQUEST;
        }

        Map<String, Object> body = createBaseBody(request, errorType);
        body.put(Nomenclature.ERR.KEY_MESSAGE, ex.getMessage());

        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Malformed JSON");
        body.put("error", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Fallback for unexpected internal server errors */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_INTERNAL);
        body.put(Nomenclature.ERR.KEY_MESSAGE, Nomenclature.MSG_INTERNAL_ERROR);
        // Log the exception for debugging
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // --- Handler methods ---

    /** POST: Robust Validation for Registration */
    private ResponseEntity<Map<String, Object>> handlePostRegistrationValidation(Map<String, String> errors, HttpServletRequest request) {
        // 1. Initialize the map with base fields (timestamp, path, error_type, module)
        Map<String, Object> body = createBaseBody(request, "Registration Failed");

        // 2. Add the specific details and validation errors
        body.put("details", String.format("Mandatory fields: %s and %s are missing or empty.",
                Nomenclature.REQD.FIRST_NAME, Nomenclature.REQD.LAST_NAME));
        body.put("validation_errors", errors);
        body.put("suggestion", "Please verify the Trainee nomenclature requirements.");

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** PUT: Validation for Profile Updates */
    private ResponseEntity<Map<String, Object>> handlePutUpdateValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Update Refused");
        body.put("details", String.format("Updating a trainee requires: %s and %s.",
                Nomenclature.REQD.USERNAME, Nomenclature.REQD.IS_ACTIVE));
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body); // TODO: Expand coverage for this method
    }

    /** PATCH: Specific Validation for Activation Toggles */
    private ResponseEntity<Map<String, Object>> handlePatchActivationValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Activation Toggle Failed");
        body.put("details", Nomenclature.REQD.IS_ACTIVE);
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    } // TODO: Expand coverage for this method

    /** GET: Handling Missing Profiles */
    private ResponseEntity<Map<String, Object>> handleGetProfileNotFound(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Profile Not Found");
        body.put("message", ex.getMessage()); // Usually already formatted by Nomenclature.getNotFoundMsg in service
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    } // TODO: Expand coverage for this method

    /** DELETE: Handling Deletion failures */
    private ResponseEntity<Map<String, Object>> handleDeleteResourceNotFound(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, "Deletion Impossible");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body); // TODO: Expand coverage for this method
    }

    // --- UTILS & FALLBACKS ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", request.getRequestURI());
        body.put("error_type", errorType);
        body.put("module", "TRAINEE_SERVICE");
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
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", Nomenclature.MSG_INTERNAL_ERROR));
    }

}