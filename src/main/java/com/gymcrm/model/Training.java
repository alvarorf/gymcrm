package com.gymcrm.model;

import lombok.Data;
import java.time.LocalDate;

@Data
public class Training {
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private TrainingType trainingType;
    private String trainingName;
    private LocalDate trainingDate;
    private Integer trainingDuration; // In minutes
}
