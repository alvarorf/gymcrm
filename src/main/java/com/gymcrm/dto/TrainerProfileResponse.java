package com.gymcrm.dto;

import com.gymcrm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Requirement 8.b: Full Trainer Profile
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerProfileResponse {
    private String firstName; // Requirement 8.b.I
    private String lastName;  // Requirement 8.b.II
    private TrainingType specialization; // Requirement 8.b.III
    private boolean isActive; // Requirement 8.b.IV
    private java.util.List<TraineeShortResponse> trainees; // Requirement 8.b.V
}
