package com.gymcrm.model;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import java.time.LocalDate;

@Data
@SuperBuilder // Ensures builder pattern works with User's fields
public class Trainee extends User {
    private LocalDate dateOfBirth;
    private String address;
}
