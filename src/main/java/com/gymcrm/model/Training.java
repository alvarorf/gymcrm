package com.gymcrm.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Data
@Entity
@Table(name = "trainings")
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder
public class Training {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Uses auto-increment at the database level
    private Long id;

    @ManyToOne
    @JoinColumn(name = "traineeId", nullable = false)
    private Trainee trainee; // Linked to Trainee entity

    @ManyToOne
    @JoinColumn(name = "trainerId", nullable = false)
    private Trainer trainer; // Linked to Trainer entity

    @ManyToOne
    @JoinColumn(name = "trainingTypeId", nullable = false)
    private TrainingType trainingType;

    @Column(nullable = false)
    private String trainingName;

    @Column(nullable = false)
    private LocalDate trainingDate; // Notes (9): Training Date, Trainee Date of Birth have Date type

    @Column(nullable = false)
    private Integer trainingDuration; // In minutes. Notes (8): Training duration has a number type.
}
