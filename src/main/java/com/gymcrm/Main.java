package com.gymcrm;

import com.gymcrm.config.AppConfig;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.gymcrm.model.Trainee;
import com.gymcrm.service.interfaces.TraineeService;

public class Main {
    public static void main(String[] args) {
        // 1. Initialize Spring Context using Java-based configuration
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        System.out.println("Spring Core context initialized.");


        // 2. Retrieve the Service bean
        TraineeService traineeService = context.getBean(TraineeService.class);

        // 3. Test Trainee Creation
        Trainee newTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .build();

        Trainee createdTrainee = traineeService.createProfile(newTrainee);
        System.out.println("\n--- Created Trainee ---");
        System.out.println("ID: " + createdTrainee.getUserId());
        System.out.println("Name: " + createdTrainee.getFirstName() + " " + createdTrainee.getLastName());
        System.out.println("Username: " + createdTrainee.getUsername());
        System.out.println("Password: " + createdTrainee.getPassword());

        // 4. Test Trainee with duplicate name
        Trainee duplicateTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .address("456 Second Ave")
                .build();

        Trainee createdDuplicate = traineeService.createProfile(duplicateTrainee);
        System.out.println("\n--- Created Duplicate Trainee ---");
        // This username should have a serial number suffix (e.g., john.doe1)
        System.out.println("Username: " + createdDuplicate.getUsername());
    }
}
