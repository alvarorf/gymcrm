package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateRequest {
    @NotBlank(message = Nomenclature.REQD.USERNAME)
    private String username;

    @NotBlank(message = Nomenclature.REQD.FIRST_NAME)
    private String firstName;

    @NotBlank(message = Nomenclature.REQD.LAST_NAME)
    private String lastName;

    private TrainingType specialization;

    @NotNull(message = Nomenclature.REQD.IS_ACTIVE)
    private Boolean isActive;
}