package com.gymcrm.dto;

import com.gymcrm.model.TrainingType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerShortResponse {
    private String username;
    private String firstName;
    private String lastName;
    private TrainingType specialization;
}
