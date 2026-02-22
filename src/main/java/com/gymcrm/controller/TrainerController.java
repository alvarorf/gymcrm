package com.gymcrm.controller;

import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.dto.*;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer", description = "Trainer management")
public class TrainerController {

    private final TrainerService trainerService;

    @Setter private TrainingService trainingService;

    public TrainerController(TrainerService trainerService) {
        this.trainerService = trainerService;
    }

    // 2. Trainer registration
    @Operation(summary = "Register a new trainer")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
      return ResponseEntity.status(HttpStatus.CREATED)
                .body(trainerService.createProfile(request));
    }

    // 8. Get Trainer profile
    @Operation(summary = "Get Trainer profile by username")
    @GetMapping("/{username}")
        public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        return trainerService.selectTrainerProfile(username)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(username)));
    }

    // Activate/de-activate trainer
    @Operation(summary = "Activate or deactivate trainer")
    @PatchMapping("/activation")
    public ResponseEntity<Void> toggleActivation(@Valid @RequestBody ActivationRequest request) {
        trainerService.toggleActivation(request.getUsername());
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update trainer profile")
    @PutMapping
    public ResponseEntity<TrainerProfileResponse> update(@Valid @RequestBody TrainerUpdateRequest request) {
        return ResponseEntity.ok(trainerService.updateProfile(request));
    }

    // 13. Get Trainer Trainings List
    @Operation(summary = "Get Trainer trainings list by criteria")
    @GetMapping("/{username}/trainings")
    public ResponseEntity<List<TrainerTrainingResponse>> getTrainerTrainings(
            @PathVariable String username,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            @RequestParam(required = false) String traineeName) {

        List<TrainerTrainingResponse> trainings = trainingService.getTrainerTrainings(username, from, to, traineeName);
        return ResponseEntity.ok(trainings);
    }
}
