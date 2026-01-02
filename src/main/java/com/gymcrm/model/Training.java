package com.gymcrm.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
public class Training {
    @Id
    private Long id;
    private Long traineeId;
    private Long trainerId;
    private TrainingType trainingType;
    @Column(nullable = false)
    private String trainingName;
    @Column(nullable = false)
    private LocalDate trainingDate; // Notes (9): Training Date, Trainee Date of Birth have Date type
    @Column(nullable = false)
    private Integer trainingDuration; // In minutes. Notes (8): Training duration has a number type.
}
