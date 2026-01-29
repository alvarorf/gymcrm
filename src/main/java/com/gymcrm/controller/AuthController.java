package com.gymcrm.controller;

import com.gymcrm.dto.PasswordChangeRequest;
import com.gymcrm.service.AuthServiceImpl;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.util.Nomenclature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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

    @Operation(summary = "3. Login")
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestParam String username, @RequestParam String password) {

        // Validation Logic
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Username and password are required."); // TODO: Use (update if necessary) Nomenclature class
        }

        try {
            // Check if UserDetails is returned
            UserDetails user = authService.authenticate(username, password);

            if (user != null) {
                return ResponseEntity.ok("Login successful"); // TODO: Use (update if necessary) Nomenclature class
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials"); // TODO: Use (update if necessary) Nomenclature class
            }
        } catch (Exception e) {
            // Handle specific authentication exceptions
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed: " + e.getMessage()); // TODO: Use (update if necessary) Nomenclature class
        }
    }

    @Operation(summary = "4. Change Login (Password)")
    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(@Valid @RequestBody PasswordChangeRequest request) {
        try {
            authService.changePassword(
                    request.getUsername(),
                    request.getOldPassword(),
                    request.getNewPassword()
            );
            return ResponseEntity.ok("Password changed successfully"); // TODO: Use (update if necessary) Nomenclature class
        } catch (Exception e) {
            // This will catch UsernameNotFoundException or BadCredentialsException
            Nomenclature.warn(logger, Nomenclature.Action.UPDATE_SENSITIVE, request.getUsername());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Failed to change password: " + e.getMessage()); // TODO: Use (update if necessary) Nomenclature class
        }
    }
}