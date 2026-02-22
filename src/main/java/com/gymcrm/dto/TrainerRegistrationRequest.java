package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerRegistrationRequest {
    @NotBlank(message = Nomenclature.REQD.FIRST_NAME)
    private String firstName;

    @NotBlank(message = Nomenclature.REQD.LAST_NAME)
    private String lastName;

    @NotNull(message = Nomenclature.REQD.SPECIALIZATION)
    private TrainingTypeRequest specialization;
}