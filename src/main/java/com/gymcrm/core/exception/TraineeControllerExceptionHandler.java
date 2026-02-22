package com.gymcrm.core.exception;

import com.gymcrm.controller.TraineeController;
import com.gymcrm.core.util.Nomenclature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice(assignableTypes = TraineeController.class)
public class TraineeControllerExceptionHandler {

    // --- 1. VALIDATION ENTRY POINT ---

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {

        String method = request.getMethod();
        String path = request.getRequestURI();
        Map<String, String> errors = extractFieldErrors(ex);

        // Routing based on the endpoint context for validation errors
        if ("PUT".equalsIgnoreCase(method)) {
            return handlePutUpdateValidation(errors, request);
        } else if ("PATCH".equalsIgnoreCase(method) && path.contains("/activation")) {
            return handlePatchActivationValidation(errors, request);
        } else if ("POST".equalsIgnoreCase(method) && path.contains("/register")) {
            return handlePostRegistrationValidation(errors, request);
        }

        // Fallback for generic validation errors
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_REG_FAILED);
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // --- 2. BUSINESS LOGIC ENTRY POINT ---

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        String method = request.getMethod();

        // Routing based on method for business exceptions (e.g. "Not Found" or "Deletion Failed")
        if ("GET".equalsIgnoreCase(method)) {
            return handleGetProfileNotFound(ex, request);
        } else if ("DELETE".equalsIgnoreCase(method)) {
            return handleDeleteResourceNotFound(ex, request);
        }

        // Default handler for logic failures (like toggle activation logic)
        String errorType = Nomenclature.ERR.TYPE_INTERNAL;
        if (request.getRequestURI().contains("/activation")) {
            errorType = Nomenclature.ERR.TYPE_TOGGLE_FAILED;
        }

        Map<String, Object> body = createBaseBody(request, errorType);
        body.put(Nomenclature.ERR.KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    // --- 3. OTHER EXCEPTIONS ---

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleMalformedJson(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_MALFORMED_JSON);
        body.put(Nomenclature.ERR.KEY_MESSAGE, ex.getMostSpecificCause().getMessage());
        body.put(Nomenclature.ERR.KEY_SUGGESTION, Nomenclature.MSG.SUGGESTION_MALFORMED);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneralException(Exception ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_INTERNAL);
        body.put(Nomenclature.ERR.KEY_MESSAGE, Nomenclature.MSG.INTERNAL_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    // --- PRIVATE HANDLER METHODS (Specialized Responses) ---

    private ResponseEntity<Map<String, Object>> handlePostRegistrationValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_REG_FAILED);
        body.put("details", String.format(Nomenclature.ERR.DETAIL_REG_MISSING,
                Nomenclature.REQD.FIRST_NAME, Nomenclature.REQD.LAST_NAME));
        body.put("validation_errors", errors);
        body.put("suggestion", Nomenclature.ERR.SUGGESTION_VERIFY_NOMEN);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> handlePutUpdateValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_UPDATE_REFUSED);
        body.put("details", String.format(Nomenclature.ERR.DETAIL_UPDATE_REQD,
                Nomenclature.REQD.USERNAME, Nomenclature.REQD.IS_ACTIVE));
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> handlePatchActivationValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_TOGGLE_FAILED);
        body.put("details", Nomenclature.REQD.IS_ACTIVE);
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    private ResponseEntity<Map<String, Object>> handleGetProfileNotFound(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_NOT_FOUND);
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    private ResponseEntity<Map<String, Object>> handleDeleteResourceNotFound(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_DEL_FAILED);
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // --- UTILS ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new LinkedHashMap<>(); // Use LinkedHashMap for ordered JSON output
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
}