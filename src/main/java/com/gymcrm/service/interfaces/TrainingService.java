package com.gymcrm.service.interfaces;

/*
Training Service class should support possibility to create/select Training profile.
 */

import com.gymcrm.dto.*;
import com.gymcrm.model.Training;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainingService {
    void createProfile(TrainingCreateRequest training);
    Optional<Training> selectProfile(Long id);
    Optional<Training> selectProfile(String trainingName);

    // 14. Get Trainee Trainings List by criteria
    List<TraineeTrainingResponse> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainerName, String type);

    // 15. Get Trainer Trainings List by criteria
    List<TrainerTrainingResponse> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName);
}