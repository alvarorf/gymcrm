package com.gymcrm.core.util;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.GeneratedCredentialsResponse;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class CredentialsGenerator {

    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom random = new SecureRandom();

    public CredentialsGenerator(TraineeDao traineeDao, TrainerDao trainerDao, PasswordEncoder passwordEncoder) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.passwordEncoder = passwordEncoder;
    }

    public GeneratedCredentialsResponse generate(String firstName, String lastName) {
        String username = generateUsername(firstName, lastName);
        String rawPassword = generateRawPassword();
        String encodedPassword = passwordEncoder.encode(rawPassword);

        return new GeneratedCredentialsResponse(username, rawPassword, encodedPassword);
    }

    private String generateUsername(String firstName, String lastName) {
        String base = (firstName.toLowerCase() + "." + lastName.toLowerCase()).replaceAll("\\s+", "");
        String unique = base;
        int serial = 0;

        while (traineeDao.findByUsername(unique).isPresent() || trainerDao.findByUsername(unique).isPresent()) {
            serial++;
            unique = base + serial;
        }
        return unique;
    }

    private String generateRawPassword() {
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
