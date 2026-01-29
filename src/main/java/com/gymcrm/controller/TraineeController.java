package com.gymcrm.controller;

import com.gymcrm.dto.*;
import com.gymcrm.model.Trainee;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.util.Nomenclature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainees")
@Tag(name = "Trainee", description = "Trainee management")
public class TraineeController {

    private final TraineeService traineeService;

    public TraineeController(TraineeService traineeService) {
        this.traineeService = traineeService;
    }

    // 1. Trainee Registration
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
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(username.getClass()))); // TODO: Possible bug, should also work with a string. Perhaps overload the method getNotFoundMsg or other solution
    }

    // 6. Update Trainee Profile
    @Operation(summary = "Update trainee profile")
    @PutMapping
    public ResponseEntity<TraineeProfileResponse> update(@Valid @RequestBody TraineeUpdateRequest request) {
        return ResponseEntity.ok(traineeService.updateProfile(request));
    }

    // 7. Delete Trainee Profile
    @Operation(summary = "Delete trainee profile")
    @DeleteMapping("/{username}")
    public ResponseEntity<Void> delete(@PathVariable String username) {
        traineeService.deleteProfile(username);
        return ResponseEntity.ok().build();
    }

    // 15. Activate/De-Activate Trainee
    @Operation(summary = "Activate or Deactivate Trainee")
    @PatchMapping("/activation")
    public ResponseEntity<Void> toggleActivation(@Valid @RequestBody TraineeActivationRequest request) {
        traineeService.toggleActivation(request.getUsername());
        return ResponseEntity.ok().build();
    }
}