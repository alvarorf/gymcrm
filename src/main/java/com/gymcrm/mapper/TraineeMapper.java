package com.gymcrm.mapper;

import com.gymcrm.dto.*;
import com.gymcrm.model.Trainee;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class TraineeMapper {

    // Maps the Trainee entity to a RegistrationResponse containing credentials
    public RegistrationResponse toRegistrationResponse(Trainee trainee) {
        return new RegistrationResponse(trainee.getUsername(), trainee.getPassword());
    }

    // Map registration request -> New entity
    public Trainee toEntity(TraineeRegistrationRequest request) {
        return Trainee.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .address(request.getAddress())
                .isActive(true)
                .build();
    }

    // Update existing entity from request
    public void updateEntityFromRequest(TraineeUpdateRequest request, Trainee existingTrainee) {
        existingTrainee.setFirstName(request.getFirstName());
        existingTrainee.setLastName(request.getLastName());
        existingTrainee.setDateOfBirth(request.getDateOfBirth());
        existingTrainee.setAddress(request.getAddress());
        existingTrainee.setActive(request.getIsActive());
    }

    // Map entity -> Profile response DTO
    public TraineeProfileResponse toProfileResponse(Trainee trainee) {
        return TraineeProfileResponse.builder()
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
                                trainer.getSpecialization()
                        ))
                        .collect(Collectors.toList()))
                .build();
    }
}