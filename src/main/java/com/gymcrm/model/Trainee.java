package com.gymcrm.model;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Data
@SuperBuilder // Ensures builder pattern works with User's fields

// So that the test class can instantiate new Trainee() and the Spring container can use it for reflection
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;
}
