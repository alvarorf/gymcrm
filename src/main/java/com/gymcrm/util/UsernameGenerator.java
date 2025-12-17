package com.gymcrm.util;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component // A bean to be managed by Spring
public class UsernameGenerator {

    // To check for existing usernames (Requirement 7)
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    // Constructor injection (because it is required that we check for existing usernames for trainer and trainee)
    public UsernameGenerator(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    /**
     * Generates a username following the rule: firstName.lastName + optional_serial_number
     */
    public String generateUsername(String firstName, String lastName) {
        String baseUsername = firstName.toLowerCase() + "." + lastName.toLowerCase();
        String uniqueUsername = baseUsername;

        int serial = 0;

        while (isUsernameTaken(uniqueUsername)) {
            serial++;
            uniqueUsername = baseUsername + serial;
        }
        return uniqueUsername;
    }

    private boolean isUsernameTaken(String username) {
        // Check both Trainee and Trainer DAOs for username existence
        return traineeDao.findByUsername(username).isPresent()
                || trainerDao.findByUsername(username).isPresent();
    }

}
