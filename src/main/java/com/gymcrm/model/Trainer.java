package com.gymcrm.model;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Data // Generates getters, setters, toString, equals/hashCode
@EqualsAndHashCode(callSuper = true)
@SuperBuilder // Required for Builder pattern inheritance
@NoArgsConstructor
@Entity
@PrimaryKeyJoinColumn(name = "userId")
public class Trainer extends User {
    @ManyToOne
    @JoinColumn(name = "typeId") // Foreign key that maps to TrainingType entity
    private String specialization;

    @OneToMany(mappedBy = "trainer")
    @ToString.Exclude
    private Set<Training> trainings;

    @ManyToMany(mappedBy = "trainers")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<Trainee> trainees;
}
