package com.gymcrm.controller;

import com.gymcrm.dto.PasswordChangeRequest;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.core.util.Nomenclature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.*;
import org.springframework.http.*;
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
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {

        // Validation: If blank, throw to be caught by AuthControllerExceptionHandler
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            throw new BadCredentialsException(Nomenclature.MSG_AUTH_REQUIRED);
        }

        // authenticate() should throw UsernameNotFoundException or BadCredentialsException internally if it fails
        authService.authenticate(username, password);

        return ResponseEntity.ok(Nomenclature.MSG_LOGIN_SUCCESS); // TODO: Improve coverage for this line

    }

    @Operation(summary = "Change login (password)")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
            authService.changePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok(Nomenclature.MSG_PASSWORD_CHANGED); // TODO: Improve coverage for this line
    }
}