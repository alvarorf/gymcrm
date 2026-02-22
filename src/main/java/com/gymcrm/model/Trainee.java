package com.gymcrm.model;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.Set;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@SuperBuilder // Ensures builder pattern works with User's fields

// So that the test class can instantiate new Trainee() and the Spring container can use it for reflection
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@Entity
@Table(name = "trainees")
@DiscriminatorValue("TRAINEE")
public class Trainee extends User {
    @Column
    private LocalDate dateOfBirth;
    @Column
    private String address;

    // Relationship: Trainees and Trainers have many-to-many relations
    @ManyToMany(fetch = FetchType.LAZY) // We need an intermediate table due to the many-to-many relationship
    @JoinTable(
            name = "trainee_trainer",
            joinColumns = @JoinColumn(name = "traineeId"),
            inverseJoinColumns = @JoinColumn(name = "trainerId")
    )
    @ToString.Exclude //  We exclude this attribute from the output of ToString, to prevent infinite recursion in logs
    @EqualsAndHashCode.Exclude
    private Set<Trainer> trainers;

    // Relationship to Trainings: One Trainee has many Trainings
    // From Notes(7): 7. Delete Trainee profile is hard deleting action and bring the cascade deletion of relevant
    // trainings
    @OneToMany(mappedBy = "trainee", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    private Set<Training> trainings;
}
