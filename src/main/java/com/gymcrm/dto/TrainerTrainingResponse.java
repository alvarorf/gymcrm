package com.gymcrm.dto;

import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerTrainingResponse {
    private String trainingName;      // Requirement 13.b.I
    private LocalDate trainingDate;   // Requirement 13.b.II
    private String trainingType;      // Requirement 13.b.III  // TODO: Perhaps there is something wrong here, shouldn't this be a TrainingType instead of String?
    private int trainingDuration;     // Requirement 13.b.IV
    private String traineeName;       // Requirement 13.b.V (Full name)
}