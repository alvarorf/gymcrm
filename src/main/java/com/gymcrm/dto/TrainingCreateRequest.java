package com.gymcrm.dto;

import com.gymcrm.util.Nomenclature;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingCreateRequest {
    @NotBlank(message = Nomenclature.REQD.TRAINEE_USERNAME)
    private String traineeUsername;

    @NotBlank(message = Nomenclature.REQD.TRAINER_USERNAME)
    private String trainerUsername;

    @NotBlank(message = Nomenclature.REQD.TRAINING_NAME)
    private String trainingName;

    @NotNull(message = Nomenclature.REQD.TRAINING_NAME)
    private LocalDate trainingDate;

    @NotNull(message = Nomenclature.REQD.TRAINING_DURATION)
    private Integer trainingDuration;
}
