package com.gymcrm.dto;

import com.gymcrm.model.TrainingType;
import jakarta.validation.constraints.*;
import lombok.*;

// Requirement 2.a: Trainer Registration Request
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerRegistrationRequest {
    @NotBlank(message = "First Name is required") // Requirement 2.a.I
    private String firstName;

    @NotBlank(message = "Last Name is required") // Requirement 2.a.II
    private String lastName;

    @NotNull(message = "Specialization is required") // Requirement 2.a.III
    private TrainingType specialization;
}