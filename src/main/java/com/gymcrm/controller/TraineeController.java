package com.gymcrm.controller;

import com.gymcrm.dto.*;
import com.gymcrm.model.Trainee;
import com.gymcrm.service.interfaces.TraineeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee", description = "Trainee Management")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    // 1. Trainee Registration
    @Operation(summary = "Register a new Trainee")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        Trainee trainee = Trainee.builder() // TODO: Move this logic to service
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .build();

        Trainee saved = traineeService.createProfile(trainee);

        // Return 201 Created with generated credentials
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(saved.getUsername(), saved.getPassword()));
    }

    // 4. Change Login
    @Operation(summary = "Change Password")
    @PutMapping("/change-login")
    public ResponseEntity<Void> changeLogin(@Valid @RequestBody ChangeLoginRequest request) {
        // Note 3: Authenticaton required
        if (!traineeService.authenticate(request.getUsername(), request.getOldPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        traineeService.selectTraineeProfile(request.getUsername()).ifPresent(t ->
                traineeService.updatePassword(t.getUserId(), request.getNewPassword())
        );

        return ResponseEntity.ok().build(); // 200 OK
    }

    @Operation(summary = "Get Trainee Profile")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) { // TODO: Handle exceptions
        return traineeService.selectTraineeProfile(username)
                .map(trainee -> ResponseEntity.ok(TraineeProfileResponse.builder()
                        .firstName(trainee.getFirstName())
                        .lastName(trainee.getLastName())
                        .dateOfBirth(trainee.getDateOfBirth())
                        .address(trainee.getAddress())
                        .isActive(trainee.isActive())
                        .trainers(trainee.getTrainers().stream()
                                .map(trainer -> new TrainerShortResponse(
                                        trainer.getUsername(),
                                        trainer.getFirstName(),
                                        trainer.getLastName(),
                                        trainer.getSpecialization() // TrainingType reference
                                ))
                                .collect(Collectors.toList()))
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }
}