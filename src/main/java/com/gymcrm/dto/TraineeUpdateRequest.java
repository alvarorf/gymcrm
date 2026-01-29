package com.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeUpdateRequest {
    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "First Name is required")
    private String firstName;

    @NotBlank(message = "Last Name is required")
    private String lastName;

    // Optional
    private LocalDate dateOfBirth;
    private String address;

    @NotNull(message = "Is Active status is required")
    private Boolean isActive;
}
