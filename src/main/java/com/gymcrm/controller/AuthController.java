package com.gymcrm.controller;

import com.gymcrm.service.interfaces.TraineeService; // Or a generic UserService
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Login management for all users")
public class AuthController {

    private final TraineeService traineeService;

    public AuthController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    @Operation(summary = "3. Login")
    @GetMapping("/login")
    public ResponseEntity<Void> login(@RequestParam String username, @RequestParam String password) {
        // Requirement 3: Return 200 OK on success
        if (traineeService.authenticate(username, password)) { // TODO: Should be a common interface for TraineeServiceImpl and TrainerServiceImpl (AuthenticationService: move the method authenticate)
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // TODO: Could be a BAD request code, so we should handle it (exception). Validate input (parameters) (validate username and password)
    }
}