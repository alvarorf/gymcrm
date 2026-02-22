package com.gymcrm.dto;

import com.gymcrm.model.TrainingType;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileNotAssignedActiveResponse {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType specialization;
    private boolean isActive;
    private List<TraineeShortResponse> trainees;
}
