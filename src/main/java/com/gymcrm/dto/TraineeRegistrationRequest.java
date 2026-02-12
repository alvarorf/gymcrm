package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import java.time.LocalDate;

@Data // Generates getters, setters, equals, hashCode, and toString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeRegistrationRequest {
    @NotBlank(message = Nomenclature.REQD.FIRST_NAME) // Requirement 1.a.I
    private String firstName;

    @NotBlank(message = Nomenclature.REQD.LAST_NAME) // Requirement 1.a.II
    private String lastName;

    // Optional
    private LocalDate dateOfBirth;
    private String address;
}
