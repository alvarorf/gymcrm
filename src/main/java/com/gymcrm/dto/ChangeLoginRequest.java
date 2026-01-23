package com.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

// Required for Requirement 4
@Data
public class ChangeLoginRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Old password is required")
    private String oldPassword;

    @NotBlank(message = "New password is required")
    private String newPassword;
}