package com.gymcrm.dto;

import com.gymcrm.core.util.Nomenclature;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTypeRequest {
    @NotBlank(message = Nomenclature.REQD.TRAINING_NAME)
    private String trainingTypeName;
}