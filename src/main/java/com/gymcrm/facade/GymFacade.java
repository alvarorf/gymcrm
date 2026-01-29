package com.gymcrm.facade;

import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import lombok.Getter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Facade class to manage service access and authentication.
 * Uses Spring Boot's managed ApplicationContext.
 */
@Component
public class GymFacade {

    private final AuthService authService;
    @Getter private final TraineeService traineeService;
    @Getter private final TrainerService trainerService;
    @Getter private final TrainingService trainingService;

    // Production constructor (used by Spring)
    public GymFacade(
                     AuthService authService,
                     TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {

        this.authService = authService;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Login logic (manual authentication)
    public boolean login(String username, String password) {
    // Delegate authentication to the AuthService
        try {
            authService.authenticate(username, password);
            return true;
        } catch (UsernameNotFoundException e) {
            return false;
        }
        }
}
