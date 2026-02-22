package com.gymcrm.mapper;

import com.gymcrm.dto.*;
import com.gymcrm.model.*;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TrainerMapper {

    // Maps the Trainer entity to a RegistrationResponse containing credentials
    public RegistrationResponse toRegistrationResponse(Trainer trainer) {
        return new RegistrationResponse(trainer.getUsername(), trainer.getPassword());
    }

    // Map registration request -> New entity
    public Trainer toEntity(TrainerRegistrationRequest request, TrainingType specialization) {
        return Trainer.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .specialization(specialization)
                .isActive(true)
                .build();
    }

    public void updateEntityFromRequest(TrainerUpdateRequest request, Trainer existingTrainer) {
        existingTrainer.setFirstName(request.getFirstName());
        existingTrainer.setLastName(request.getLastName());
        existingTrainer.setActive(request.getIsActive());
        // Specialization is read-only per requirement 9.a.IV, so we don't update it.
    }

    public TrainerProfileResponse toProfileResponse(Trainer trainer) {
        return TrainerProfileResponse.builder()
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .specialization(trainer.getSpecialization())
                .isActive(trainer.isActive())
                .trainees(trainer.getTrainees() == null ? null : trainer.getTrainees().stream()
                        .map(this::toTraineeShortResponse)
                        .collect(Collectors.toList()))
                .build();
    }

    public TraineeShortResponse toTraineeShortResponse(Trainee trainee) {
        return TraineeShortResponse.builder()
                .username(trainee.getUsername())
                .firstName(trainee.getFirstName())
                .lastName(trainee.getLastName())
                .build();
    }

    public TrainerShortResponse toShortResponse(Trainer trainer) {
        return TrainerShortResponse.builder()
                .username(trainer.getUsername())
                .firstName(trainer.getFirstName())
                .lastName(trainer.getLastName())
                .specialization(trainer.getSpecialization())
                .build();
    }
}
