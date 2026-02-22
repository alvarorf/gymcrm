package com.gymcrm.controller;

import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.dto.LoginRequest;
import com.gymcrm.dto.PasswordChangeRequest;
import com.gymcrm.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login management for all users")
public class AuthController {

    private final AuthService authService;
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login")
    @PostMapping(value = "/login", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() || request.getPassword() == null || request.getPassword().isBlank()) {
            throw new BadCredentialsException(Nomenclature.MSG.AUTH_REQUIRED);
        }

        // We capture the token even if we return a success message
        String token = authService.authenticate(request.getUsername(), request.getPassword());

        // Ensure the message is explicitly in the body
        return ResponseEntity.ok().body(Nomenclature.MSG.LOGIN_SUCCESS);
    }

    @Operation(summary = "Logout")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        // The actual logic is handled by SecurityConfig filter chain.
        // This method serves as an entry point for documentation and explicit mapping.
        return ResponseEntity.ok(Nomenclature.MSG.LOGOUT_SUCCESS);
    }

    @Operation(summary = "Change login (password)")
    @PutMapping(value = "/change-password", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> changePassword(@NotNull @Valid @RequestBody PasswordChangeRequest request) {
        authService.changePassword(
                request.getUsername(),
                request.getOldPassword(),
                request.getNewPassword()
        );
        return ResponseEntity.ok().body(Nomenclature.MSG.PASSWORD_CHANGED);
    }
}