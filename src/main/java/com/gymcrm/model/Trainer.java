package com.gymcrm.model;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data // Generates getters, setters, toString, equals/hashCode
@EqualsAndHashCode(callSuper = true)
@SuperBuilder // Required for Builder pattern inheritance
@NoArgsConstructor
@Entity
@Table(name = "trainers")
@DiscriminatorValue("TRAINER")
public class Trainer extends User {
    @ManyToOne
    @JoinColumn(name = "trainingTypeId", nullable = false) // Foreign key that maps to TrainingType entity
    private TrainingType specialization;

    @OneToMany(mappedBy = "trainer")
    @ToString.Exclude
    private Set<Training> trainings;

    @ManyToMany(mappedBy = "trainers")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Trainee> trainees;
}
