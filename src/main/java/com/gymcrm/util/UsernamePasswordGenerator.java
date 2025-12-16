package com.gymcrm.util;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component // A bean to be managed by Spring
public class UsernamePasswordGenerator {

    // To check for existing usernames (Requirement 7)
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;

    // Constructor injection (because it is required that we check for existing usernames for trainer and trainee)
    public UsernamePasswordGenerator(TraineeDao traineeDao, TrainerDao trainerDao) {
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

    /**
     * Generates a random 10-character password, according to requirement 7, bullet point 3:
     * "Password should be generated as a random 10 chars length string."
     */

    public String generatePassword(){
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            // From 0 to 9, so length is 10 characters
            // It randomly selects one character from "chars" at each iteratiom, and appends them
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

}
