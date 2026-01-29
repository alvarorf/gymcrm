package com.gymcrm.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data // Generates getters, setters, equals, hashCode, and toString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeRegistrationRequest {
    @NotBlank(message = "First Name is required") // Requirement 1.a.I
    private String firstName;

    @NotBlank(message = "Last Name is required") // Requirement 1.a.II
    private String lastName;

    // Optional
    private LocalDate dateOfBirth;
    private String address;
}
