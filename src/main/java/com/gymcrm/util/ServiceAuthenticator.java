package com.gymcrm.util;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ServiceAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(ServiceAuthenticator.class);

    private TraineeDao traineeDao;
    private TrainerDao trainerDao;

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) { this.traineeDao = traineeDao; }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }

    /**
     * Validates credentials against both Trainee and Trainer tables.
     * Throws SecurityException if authentication fails.
     */

    public void validate(String username, String password) {
        boolean isValidTrainee = traineeDao.findByUsername(username)
                .map(t -> t.getPassword().equals(password)).orElse(false);

        boolean isValidTrainer = trainerDao.findByUsername(username)
                .map(t -> t.getPassword().equals(password)).orElse(false);

        if (!isValidTrainee && !isValidTrainer) {
            logger.error("Authentication failed for user: {}", username);
            throw new SecurityException("Access Denied: Invalid credentials.");
        }
        logger.debug("Authentication successful for user: {}", username);
    }
}
