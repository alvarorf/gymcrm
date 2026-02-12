package com.gymcrm.controller;

import com.gymcrm.dto.*;
import com.gymcrm.service.interfaces.*;
import com.gymcrm.core.util.Nomenclature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee", description = "Trainee management")
public class TraineeController {

    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TraineeController(TraineeService traineeService, TrainerService trainerService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    // 1. Trainee registration
    @Operation(summary = "Register a new trainee")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TraineeRegistrationRequest request) {
        RegistrationResponse response = traineeService.createProfile(request);

        // Return 201 Created with generated credentials
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Get trainee profile")
    @GetMapping("/{username}")
    public ResponseEntity<TraineeProfileResponse> getProfile(@PathVariable String username) {
        return traineeService.selectTraineeProfile(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(username)));
    } // TODO: Expand coverage for this method

    // 6. Update trainee profile
    @Operation(summary = "Update trainee profile")
    @PutMapping
    public ResponseEntity<TraineeProfileResponse> update(@Valid @RequestBody TraineeUpdateRequest request) {
        return ResponseEntity.ok(traineeService.updateProfile(request));
    }

    // 7. Delete trainee profile
    @Operation(summary = "Delete trainee profile")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        traineeService.deleteProfile(username);
        return ResponseEntity.ok().build();
    } // TODO: Expand coverage for this method

    // Activate/de-activate trainee
    @Operation(summary = "Activate or deactivate Trainee")
    @PatchMapping("/activation")
    public ResponseEntity<Void> toggleActivation(@Valid @RequestBody ActivationRequest request) {
        traineeService.toggleActivation(request.getUsername());
        return ResponseEntity.ok().build(); // TODO: Expand coverage for this method
    }

    // 10. Get not assigned on trainee active trainers
    @Operation(summary = "Get active trainers not assigned to the given trainee")
    @GetMapping("/{username}/unassigned-trainers")
    public ResponseEntity<List<TrainerShortResponse>> getUnassignedTrainers(@PathVariable String username) {
        return ResponseEntity.ok(trainerService.getUnassignedActiveTrainersByTraineeUsername(username)); // TODO: Expand coverage for this method
    }
}