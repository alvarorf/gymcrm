package com.gymcrm.model;

import jakarta.persistence.*;
import lombok.Data;

@Data //  Bundles the features of @ToString, @EqualsAndHashCode, @Getter / @Setter and @RequiredArgsConstructor together:
@Entity
public class TrainingType {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false)
    private String trainingTypeName;
}
