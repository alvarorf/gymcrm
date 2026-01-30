package com.gymcrm.util;

import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TrainerProfileResponse;
import com.gymcrm.facade.GymFacade;
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

        TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                .firstName("John")
                .lastName("Doe")
                .address("123 Main St")
                .build();

        RegistrationResponse createdTrainee = gym.getTraineeService().createProfile(request);
        System.out.println("Created trainee: " + createdTrainee.getUsername());

        Optional<TrainerProfileResponse> trainer = gym.getTrainerService().selectTrainerProfile(101L);
        trainer.ifPresent(t ->
                System.out.println("Trainer: " + t.getFirstName())
        );
    }
}
