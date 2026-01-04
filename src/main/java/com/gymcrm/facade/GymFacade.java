package com.gymcrm.facade;

import com.gymcrm.config.AppConfig;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import java.util.Collections;

/**
 * Facade class to manage service access and context initialization.
 */
public class GymFacade {
    private final ApplicationContext context;

    public GymFacade() {
        this.context = new AnnotationConfigApplicationContext(AppConfig.class);
    }

    /**
     * Manually sets the SecurityContext for the current thread.
     * The equivalent of "logging in".
     */
    public boolean login(String username, String password) {
        // Use services to check credentials
        boolean isTrainee = getTraineeService().authenticate(username, password);
        boolean isTrainer = getTrainerService().authenticate(username, password);

        if (isTrainee || isTrainer) {
            // Create a Spring Security authentication token
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    username, password, Collections.emptyList());

            // Populate the context - this allows @PreAuthorize calls to pass
            SecurityContextHolder.getContext().setAuthentication(auth);
            return true;
        }
        return false;
    }

    public TraineeService getTraineeService() {
        return context.getBean(TraineeService.class);
    }

    public TrainerService getTrainerService() {
        return context.getBean(TrainerService.class);
    }

    public TrainingService getTrainingService() {
        return context.getBean(TrainingService.class);
    }


}
