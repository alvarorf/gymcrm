package com.gymcrm.util;

import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CrmDemoRunner {

    public void runDemo(GymFacade gym) {
        // Manually set an Authentication object ---
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                "system_admin", null, AuthorityUtils.createAuthorityList("ROLE_ADMIN"));
        SecurityContextHolder.getContext().setAuthentication(auth);

        System.out.println("--- Gym CRM system initialized via Facade ---");

        // Test Trainee Creation
        Trainee newTrainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .build();

        Trainee createdTrainee = gym.getTraineeService().createProfile(newTrainee);
        System.out.println("\n--- Created Trainee ---");
        System.out.println("Username: " + createdTrainee.getUsername());

        // Demonstrate Trainer retrieval (using data loaded from JSON)
        Optional<Trainer> trainer = gym.getTrainerService().selectProfile(101L);
        trainer.ifPresent(t -> {
            System.out.println("\n--- Loaded initial trainer ---");
            System.out.println("Name: " + t.getFirstName() + " " + t.getLastName());
            System.out.println("Specialization: " + t.getSpecialization());
        });
    }
}