package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeUpdateRequest {
    @NotBlank(message = Nomenclature.REQD.USERNAME)
    private String username;

    @NotBlank(message = Nomenclature.REQD.FIRST_NAME)
    private String firstName;

    @NotBlank(message = Nomenclature.REQD.LAST_NAME)
    private String lastName;

    // Optional
    private LocalDate dateOfBirth;
    private String address;

    @NotNull(message = Nomenclature.REQD.IS_ACTIVE)
    private Boolean isActive;
}
