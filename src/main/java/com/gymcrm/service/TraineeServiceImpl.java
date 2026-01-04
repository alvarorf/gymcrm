package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
Trainee Service class should support possibility to create/update/delete/select Trainee
profile.
*/


/*
From: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Service.html
@Service:
Indicates that an annotated class is a "Service", originally defined by Domain-Driven Design (Evans, 2003)
as "an operation offered as an interface that stands alone in the model, with no encapsulated state."
It is a specialization (implementation) of @Component and allows TraineeServiceImpl to be autodetected through classpath scanning.
 */
@Service
public class TraineeServiceImpl implements TraineeService {
    // Why final? Because TraineeDao is a core dependency, injected via the constructor
    private final TraineeDao traineeDao;
    // Non-Core Dependencies. Must NOT be final, for injection via Setter
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerDao trainerDao;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    // Constructor-based injection (only for core dependencies)
    public TraineeServiceImpl(TraineeDao traineeDao)
    {
        this.traineeDao = traineeDao;
        logger.info("TraineeServiceImpl initialized with constructor injection for DAO.");
    }

    // Setter-based injection for the non-core dependencies
    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) { this.usernameGenerator = usernameGenerator; }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) { this.passwordGenerator = passwordGenerator; }
    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }

    @Override
    public Trainee createProfile(Trainee trainee) {
        logger.info("Attempting to create new Trainee profile: {} {}", trainee.getFirstName(), trainee.getLastName());
        String username = usernameGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = passwordGenerator.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(password);

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee created successfully. Username: {}", savedTrainee.getUsername());
        return savedTrainee;
    }

    @Override
    public boolean authenticate(String username, String password) {
        logger.debug("Authenticating trainee: {}", username);
        return traineeDao.findByUsername(username)
                .map(trainee -> trainee.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Trainee updateProfile(Trainee trainee) {
        logger.info("Attempting to update Trainee profile with ID: {}", trainee.getUserId());
        // Notes (3): Required field validation
        if (trainee.getFirstName() == null || trainee.getLastName() == null) {
            throw new IllegalArgumentException("First Name and Last Name are required.");
        }
        return traineeDao.save(trainee);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Trainee> selectProfile(String targetUser) {

        logger.info("Selecting Trainee profile by username: {}", targetUser);
        return traineeDao.findByUsername(targetUser);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Trainee> selectProfile(Long id) {
        logger.info("Selecting Trainee profile by ID: {}", id);
        return traineeDao.findById(id);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteProfile(Long id) {
        logger.warn("Attempting to delete Trainee profile with ID: {}", id);
        traineeDao.delete(id);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteProfile(String targetUser) {

        logger.warn("Attempting to delete Trainee profile with username: {}", targetUser);
        // 13: Delete by username
        traineeDao.findByUsername(targetUser).ifPresent(t -> traineeDao.delete(t.getUserId()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(Long id, String newPassword) {
        traineeDao.findById(id).ifPresent(trainee -> {
            trainee.setPassword(newPassword);
            traineeDao.save(trainee);
            logger.info("Password updated for Trainee ID: {}", id);
        });
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void toggleActivation(Long id) {
        traineeDao.findById(id).ifPresent(trainee -> {
            trainee.setActive(!trainee.isActive());
            traineeDao.save(trainee);
            logger.info("Trainee activation status changed to: {}", trainee.isActive());
        });
    }

    // 18. Update Trainee's trainers list
    @Override
    @PreAuthorize("isAuthenticated()")
    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));

        Set<Trainer> newTrainers = trainerUsernames.stream()
                .map(u -> trainerDao.findByUsername(u).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        trainee.setTrainers(newTrainers);
        traineeDao.save(trainee);
        logger.info("Updated trainer list for trainee: {}", traineeUsername);
    }
}
