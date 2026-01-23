package com.gymcrm.controller;

import com.gymcrm.service.interfaces.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login management for all users")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "3. Login")
    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) {
        // Validation logic
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return ResponseEntity.badRequest().build();
        }

        // Requirement 3: Return 200 OK on success
        if (authService.authenticate(username, password)) {
            return ResponseEntity.ok().build();
        }


        if (authService.authenticate(username, password)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // TODO: Could be a BAD request code, so we should handle it (exception). Validate input (parameters) (validate username and password)
    }
}