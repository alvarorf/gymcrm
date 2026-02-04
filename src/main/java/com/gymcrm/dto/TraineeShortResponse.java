package com.gymcrm.dto;

import lombok.*;

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