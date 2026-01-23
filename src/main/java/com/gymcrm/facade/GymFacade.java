package com.gymcrm.facade;

import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import org.springframework.context.ApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * Facade class to manage service access and authentication.
 * Uses Spring Boot's managed ApplicationContext.
 */
@Component
public class GymFacade {

    private final ApplicationContext context;
    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    // Production constructor (used by Spring)
    public GymFacade(ApplicationContext context,
                     TraineeService traineeService,
                     TrainerService trainerService,
                     TrainingService trainingService) {

        this.context = context;
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    // Login logic (manual authentication)
    public boolean login(String username, String password) {

        if (traineeService.authenticate(username, password)
                || trainerService.authenticate(username, password)) {

            Authentication auth =
                    new UsernamePasswordAuthenticationToken(
                            username,
                            password,
                            Collections.emptyList()
                    );

            SecurityContextHolder.getContext().setAuthentication(auth);
            return true;
        }

        return false;
    }

    // Getter access

    public ApplicationContext getContext() {
        return context;
    }

    public TraineeService getTraineeService() {
        return traineeService;
    }

    public TrainerService getTrainerService() {
        return trainerService;
    }

    public TrainingService getTrainingService() {
        return trainingService;
    }
}
