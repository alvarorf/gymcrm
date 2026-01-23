package com.gymcrm.util;

import com.gymcrm.facade.GymFacade;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class CrmDemoRunner implements ApplicationRunner {

    private final GymFacade gym;

    public CrmDemoRunner(GymFacade gym) {
        this.gym = gym;
    }

    @Override
    public void run(ApplicationArguments args) {

        // Fake login
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        "system_admin", null,
                        AuthorityUtils.createAuthorityList("ROLE_ADMIN")
                )
        );

        System.out.println("=== Gym CRM system initialized ===");

        Trainee trainee = Trainee.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .build();

        Trainee created = gym.getTraineeService().createProfile(trainee);
        System.out.println("Created trainee: " + created.getUsername());

        Optional<Trainer> trainer = gym.getTrainerService().selectProfile(101L);
        trainer.ifPresent(t ->
                System.out.println("Trainer: " + t.getFirstName())
        );
    }
}
