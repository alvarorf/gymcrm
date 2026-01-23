package com.gymcrm.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// Requirement 8.b.V: Trainee details inside Trainer profile
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TraineeShortResponse {
    private String username;
    private String firstName;
    private String lastName;
}