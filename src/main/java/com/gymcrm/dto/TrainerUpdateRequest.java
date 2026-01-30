package com.gymcrm.dto;

import com.gymcrm.model.TrainingType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    private TrainingType specialization;

    @NotNull(message = "Is Active status is required")
    private Boolean isActive;
}