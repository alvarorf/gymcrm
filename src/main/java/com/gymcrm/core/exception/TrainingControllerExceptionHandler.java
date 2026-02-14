package com.gymcrm.core.exception;

import com.gymcrm.controller.TrainingController;
import com.gymcrm.core.util.Nomenclature;
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

    /** Handle @Valid failures (empty fields, blank strings) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = extractFieldErrors(ex);
        // TrainingController currently only has POST, so we delegate to POST logic
        return handlePostTrainingValidation(errors, request);
    }

    /** Handle incorrect data types (e.g., sending "abc" for duration/date) or malformed JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleSerializationError(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return handleIncorrectDataTypes(ex, request);
    }

    /** Handle service layer logic errors (e.g., trainee/trainer username not found) */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(RuntimeException ex, HttpServletRequest request) {
        return handleTrainingBusinessLogicError(ex, request);
    }

    // --- Handler methods ---


    /** POST: Robust validation for training creation */
    private ResponseEntity<Map<String, Object>> handlePostTrainingValidation(Map<String, String> errors, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_TRAINING_DENIED);
        body.put(Nomenclature.ERR.KEY_DETAILS, Nomenclature.ERR.DETAIL_TRAINING_MISSING);
        body.put("validation_errors", errors);
        body.put(Nomenclature.ERR.KEY_SUGGESTION, Nomenclature.ERR.SUGGESTION_TRAINING_REQD);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** Specific Handler for type mismatches (String vs Integer/Date) */
    private ResponseEntity<Map<String, Object>> handleIncorrectDataTypes(HttpMessageNotReadableException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_INVALID_FORMAT);
        body.put(Nomenclature.ERR.KEY_DETAILS, Nomenclature.ERR.DETAIL_INCOMPATIBLE_TYPES);
        body.put(Nomenclature.ERR.KEY_MESSAGE, Nomenclature.ERR.SUGGESTION_FORMAT);
        body.put("technical_error", ex.getMostSpecificCause().getMessage());
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(body);
    }

    /** GET/POST: Business logic failures (e.g., usernames not found) */
    private ResponseEntity<Map<String, Object>> handleTrainingBusinessLogicError(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_PERSISTENCE);
        body.put(Nomenclature.ERR.KEY_MESSAGE, ex.getMessage());
        body.put(Nomenclature.ERR.KEY_SUGGESTION, Nomenclature.ERR.SUGGESTION_USER_VERIFY);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    // --- UTILITIES ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("path", request.getRequestURI());
        body.put(Nomenclature.ERR.KEY_ERR_TYPE, errorType);
        body.put("module", Nomenclature.ERR.MODULE_TRAINING);
        return body;
    }

    private Map<String, String> extractFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return fieldErrors;
    }
}