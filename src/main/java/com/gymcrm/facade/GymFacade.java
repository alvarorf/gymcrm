package com.gymcrm.facade;

import com.gymcrm.config.AppConfig;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Facade class to manage service access and context initialization.
 */
public class GymFacade {
    private final ApplicationContext context;

    public GymFacade() {
        this.context = new AnnotationConfigApplicationContext(AppConfig.class);
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
