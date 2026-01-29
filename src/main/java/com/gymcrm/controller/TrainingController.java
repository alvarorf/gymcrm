package com.gymcrm.controller;

import com.gymcrm.dto.TrainingCreateRequest;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import com.gymcrm.util.Nomenclature;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/trainings")
@Tag(name = "Training", description = "Training Management")
public class TrainingController {

    private final TrainingService trainingService;
    private final TraineeService traineeService;
    private final TrainerService trainerService;

    public TrainingController(TrainingService trainingService,
                              TraineeService traineeService,
                              TrainerService trainerService) {
        this.trainingService = trainingService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
    }

    @Operation(summary = "Add a new Training session")
    @PostMapping
    public ResponseEntity<Void> addTraining(@Valid @RequestBody TrainingCreateRequest request) {
        // 1. Resolve Trainee
        Trainee trainee = traineeService.selectTraineeProfile(request.getTraineeUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(Trainee.class)));

        // 2. Resolve Trainer
        Trainer trainer = trainerService.selectProfile(request.getTrainerUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(Trainer.class)));

        // 3. Map to Entity //
        Training training = Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .trainingType(trainer.getSpecialization()) // Inherit type from trainer's specialization
                .build();

        trainingService.createProfile(training);
        return ResponseEntity.ok().build();
    }
}