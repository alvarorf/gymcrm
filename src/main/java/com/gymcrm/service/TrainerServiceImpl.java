package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service  // Could also be @Component
public class TrainerServiceImpl implements TrainerService {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    // Dependency injected via constructor (because, by req4:
    // "DAO with storage bean should be inserted into services beans using auto wiring")
    private final TrainerDao trainerDao;

    // Non-Core Dependencies. Must NOT be final, for injection via Setter
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TraineeDao traineeDao;

    // Constructor-based injection, we only inject TrainerDao because it is a core dependency

    public TrainerServiceImpl(TrainerDao trainerDao)
    {
        this.trainerDao = trainerDao;
        logger.info("TrainerServiceImpl initialized with constructor injection for DAO.");
    }

    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) { this.traineeDao = traineeDao; }

    @Override
    public Trainer createProfile(Trainer trainer)
    {
        logger.info("Attempting to create new Trainer profile: {} {}", trainer.getFirstName(), trainer.getLastName());
        String username = usernameGenerator.generateUsername(trainer.getFirstName(), trainer.getLastName());
        String password = passwordGenerator.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(password);

        Trainer savedTrainer = trainerDao.save(trainer);
        logger.info("Trainer created successfully. Username: {}", savedTrainer.getUsername());
        return savedTrainer;
    }

    @Override
    public boolean authenticate(String username, String password) {
        logger.debug("Authenticating trainer: {}", username);
        return trainerDao.findByUsername(username)
                .map(trainer -> trainer.getPassword().equals(password))
                .orElse(false);
    }

    // For 17. We need to find trainers who are not currently associated with a specific trainee
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Trainer> getUnassignedTrainersByTraineeUsername(String traineeUsername) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new RuntimeException("Trainee not found"));

        List<Trainer> allTrainers = trainerDao.findAll();

        // Filter out trainers already in the trainee's list
        return allTrainers.stream()
                .filter(trainer -> trainee.getTrainers() == null || !trainee.getTrainers().contains(trainer))
                .collect(Collectors.toList());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Trainer updateProfile(Trainer trainer)
    {
        logger.info("Attempting to update Trainer profile with ID: {}", trainer.getUserId());
        // Notes (3): Required field validation
        if (trainer.getFirstName() == null || trainer.getLastName() == null) {
            throw new IllegalArgumentException("First Name and Last Name are required.");
        }
        return trainerDao.save(trainer);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Trainer> selectProfile(Long id)
    {
        logger.info("Attempting to select Trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Trainer> selectProfile(String username) {

        logger.info("Selecting Trainer profile by username: {}", username);
        return trainerDao.findByUsername(username);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(Long id, String newPassword) {
        trainerDao.findById(id).ifPresent(trainer -> {
            trainer.setPassword(newPassword);
            trainerDao.save(trainer);
            logger.info("Password updated for Trainer ID: {}", id);
        });
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void toggleActivation(Long id) {
        trainerDao.findById(id).ifPresent(trainer -> {
            // Req 6: Non-idempotent (Toggles the current state)
            trainer.setActive(!trainer.isActive());
            trainerDao.save(trainer);
            logger.info("Trainer activation status changed to: {}", trainer.isActive());
        });
    }
}
