package com.gymcrm.dto;

import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainingTypeRequest {
    private String trainingTypeName;
}