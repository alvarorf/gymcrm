package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor // Required for Jackson
@AllArgsConstructor
public class ActivationRequest {
    @NotBlank(message = Nomenclature.REQD.LAST_NAME)
    private String username;

    @NotNull(message = Nomenclature.REQD.IS_ACTIVE)
    private Boolean isActive;
}