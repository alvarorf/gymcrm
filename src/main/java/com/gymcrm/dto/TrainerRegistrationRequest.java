package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerRegistrationRequest {
    @NotBlank(message = Nomenclature.REQD.FIRST_NAME) // Requirement 2.a.I
    private String firstName;

    @NotBlank(message = Nomenclature.REQD.LAST_NAME) // Requirement 2.a.II
    private String lastName;

    @NotNull(message = Nomenclature.REQD.SPECIALIZATION) // Requirement 2.a.III
    private TrainingTypeRequest specialization;
}