package com.gymcrm.controller;

import com.gymcrm.dto.*;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/trainers")
@Tag(name = "Trainer", description = "Trainer management")
public class TrainerController {

    private final TrainerService trainerService;
    private final TrainingTypeService trainingTypeService;

    public TrainerController(TrainerService trainerService, TrainingTypeService trainingTypeService) {
        this.trainerService = trainerService;
        this.trainingTypeService = trainingTypeService;
    }

    // 2. Trainer registration
    @Operation(summary = "Register a new trainer")
    @PostMapping("/register")
    public ResponseEntity<RegistrationResponse> register(@Valid @RequestBody TrainerRegistrationRequest request) {
        // 1. Look up the specialization entity by name
        TrainingType specialization = trainingTypeService.findByName(request.getSpecialization().getTrainingTypeName())
                .orElseThrow(() -> new RuntimeException("Training Type not found")); // TODO: Use the Nomenclature class here
        // Build entity using SuperBuilder   // TODO: Move this to the service and perhaps make the service use another class, some kind of mapper
        Trainer trainer = Trainer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(specialization) // Pass the Entity, not the String
                .isActive(true)
                .build();

        Trainer saved = trainerService.createProfile(trainer);

        // Requirement 2.b: Return credentials
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new RegistrationResponse(saved.getUsername(), saved.getPassword()));
    }

    // 8. Get Trainer profile
    @Operation(summary = "Get Trainer profile by username")
    @GetMapping("/{username}")
    public ResponseEntity<TrainerProfileResponse> getProfile(@PathVariable String username) {
        return trainerService.selectProfile(username)
                .map(trainer -> ResponseEntity.ok(TrainerProfileResponse.builder()
                        .firstName(trainer.getFirstName())
                        .lastName(trainer.getLastName())
                        .specialization(trainer.getSpecialization())
                        .isActive(trainer.isActive())
                        .trainees(trainer.getTrainees().stream()
                                .map(t -> new TraineeShortResponse(t.getUsername(), t.getFirstName(), t.getLastName()))
                                .collect(Collectors.toList()))
                        .build()))
                .orElse(ResponseEntity.notFound().build());
    }

    // 15. Activate/De-Activate Trainee
    @Operation(summary = "Activate or Deactivate Trainer")
    @PatchMapping("/activation")
    public ResponseEntity<Void> toggleActivation(@Valid @RequestBody TraineeActivationRequest request) {
        trainerService.toggleActivation(request.getUsername());
        return ResponseEntity.ok().build();
    }


}
