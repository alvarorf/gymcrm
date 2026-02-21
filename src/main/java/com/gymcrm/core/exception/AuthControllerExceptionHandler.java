package com.gymcrm.core.exception;

import com.gymcrm.controller.AuthController;
import com.gymcrm.core.util.Nomenclature;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestControllerAdvice(assignableTypes = AuthController.class)
public class AuthControllerExceptionHandler {

    /** 1. Handles @Valid failures (empty credentials/passwords) */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));

        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_AUTH_FAILED);
        body.put(Nomenclature.ERR.KEY_DETAILS, Nomenclature.MSG.AUTH_REQUIRED);
        body.put("validation_errors", errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }

    /** 2. Handles wrong password */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(BadCredentialsException ex, HttpServletRequest request) {
        boolean isMissingInput = ex.getMessage() != null && ex.getMessage().contains(Nomenclature.MSG.AUTH_REQUIRED);
        HttpStatus status = isMissingInput ? HttpStatus.BAD_REQUEST : HttpStatus.UNAUTHORIZED;

        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_CREDENTIALS_INVALID);
        body.put(Nomenclature.ERR.KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(status).body(body);
    }

    /** 3. Handles non-existing username */
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFound(UsernameNotFoundException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_USER_NOT_FOUND);
        body.put(Nomenclature.ERR.KEY_MESSAGE, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    /** 4. Handles business logic (e.g., old password same as new one) */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArguments(IllegalArgumentException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_SAME_PASSWORD);
        body.put(Nomenclature.ERR.KEY_MESSAGE, Nomenclature.MSG.PASSWORD_CHANGE_FAILED);
        body.put(Nomenclature.ERR.KEY_DETAILS, ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    /** 5. Catch-all for auth runtime issues */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleGeneralAuthRuntime(RuntimeException ex, HttpServletRequest request) {
        Map<String, Object> body = createBaseBody(request, Nomenclature.ERR.TYPE_AUTH_FAILED);
        body.put(Nomenclature.ERR.KEY_MESSAGE, Nomenclature.MSG.PASSWORD_CHANGE_FAILED);
        body.put("technical_details", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
    }

    // --- Utilities ---

    private Map<String, Object> createBaseBody(HttpServletRequest request, String errorType) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("path", request.getRequestURI());
        body.put(Nomenclature.ERR.KEY_ERR_TYPE, errorType);
        body.put("module", Nomenclature.ERR.MODULE_AUTH);
        return body;
    }

    private Map<String, String> extractFieldErrors(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.put(error.getField(), error.getDefaultMessage()));
        return fieldErrors;
    }
}