package com.gymcrm.model;

import org.hibernate.annotations.Immutable;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data //  Bundles the features of @ToString, @EqualsAndHashCode, @Getter / @Setter and @RequiredArgsConstructor together:
@Entity
@Immutable // Prevents Hibernate from ever trying to update this record
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@SuperBuilder
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String trainingTypeName;
}
