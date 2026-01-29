package com.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TraineeActivationRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotNull(message = "Is Active is required")
    private Boolean isActive;
}