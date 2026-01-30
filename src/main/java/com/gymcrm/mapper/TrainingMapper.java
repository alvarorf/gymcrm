package com.gymcrm.mapper;

import com.gymcrm.dto.TraineeTrainingResponse;
import com.gymcrm.dto.TrainerTrainingResponse;
import com.gymcrm.dto.TrainingCreateRequest;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import org.springframework.stereotype.Component;

@Component
public class TrainingMapper {

    public Training toEntity(Trainee trainee, Trainer trainer, TrainingCreateRequest request) {
        return Training.builder()
                .trainee(trainee)
                .trainer(trainer)
                .trainingName(request.getTrainingName())
                .trainingDate(request.getTrainingDate())
                .trainingDuration(request.getTrainingDuration())
                .trainingType(trainer.getSpecialization()) // Inherited
                .build();
    }

    public TrainerTrainingResponse toTrainerTrainingResponse(Training training) {
        return TrainerTrainingResponse.builder()
                .trainingName(training.getTrainingName())
                .trainingDate(training.getTrainingDate())
                .trainingType(training.getTrainingType().getTrainingTypeName())
                .trainingDuration(training.getTrainingDuration())
                .traineeName(training.getTrainee().getFirstName() + " " + training.getTrainee().getLastName())
                .build();
    }

    public TraineeTrainingResponse toTraineeTrainingResponse(Training training) {
        return TraineeTrainingResponse.builder()
                .trainingName(training.getTrainingName())
                .trainingDate(training.getTrainingDate())
                .trainingType(training.getTrainingType().getTrainingTypeName())
                .trainingDuration(training.getTrainingDuration())
                .trainerName(training.getTrainer().getFirstName() + " " + training.getTrainer().getLastName())
                .build();
    }
}