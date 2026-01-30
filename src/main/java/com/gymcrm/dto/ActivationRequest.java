package com.gymcrm.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class ActivationRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Is Active is required")
    private Boolean isActive;
}