package com.gymcrm.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeTrainingResponse {
    private String trainingName;      // Requirement 14.b.I
    private LocalDate trainingDate;   // Requirement 14.b.II
    private String trainingType;      // Requirement 14.b.III
    private int trainingDuration;     // Requirement 14.b.IV
    private String trainerName;       // Requirement 14.b.V (Full Name)
}