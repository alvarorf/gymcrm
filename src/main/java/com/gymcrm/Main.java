package com.gymcrm;

import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;

import java.util.Optional;

public class Main {
    public static void main(String[] args) {
        // Initialize the Facade (which handles Spring Context internally)
        GymFacade gym = new GymFacade();
        System.out.println("--- Gym CRM System Initialized via Facade ---");

        // Retrieve services through the Facade
        TraineeService traineeService = gym.getTraineeService();
        TrainerService trainerService = gym.getTrainerService();

        // Test Trainee Creation
        Trainee newTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .build();

        Trainee createdTrainee = traineeService.createProfile(newTrainee);
        System.out.println("\n--- Created Trainee ---");
        System.out.println("Username: " + createdTrainee.getUsername()); // Suffix logic handled by service

        // Demonstrate Trainer retrieval (using data loaded from JSON)
        Optional<Trainer> trainer = trainerService.selectProfile(101L);
        trainer.ifPresent(t -> {
            System.out.println("\n--- Loaded Initial Trainer ---");
            System.out.println("Name: " + t.getFirstName() + " " + t.getLastName());
            System.out.println("Specialization: " + t.getSpecialization());
        });
    }
}