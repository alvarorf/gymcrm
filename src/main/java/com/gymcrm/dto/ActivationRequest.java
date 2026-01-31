package com.gymcrm.dto;

import com.gymcrm.util.Nomenclature;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ActivationRequest {
    @NotBlank(message = Nomenclature.REQD.LAST_NAME)
    private String username;

    @NotNull(message = Nomenclature.REQD.IS_ACTIVE)
    private Boolean isActive;
}