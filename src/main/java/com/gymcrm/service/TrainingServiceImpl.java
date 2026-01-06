package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.service.interfaces.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service  // Could also be @Component
public class TrainingServiceImpl implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    // Core Dependency: DAO, injected via constructor
    private final TrainingDao trainingDao;

    // Non-core dependencies (needed for username filtering), injected via setters
    private TraineeDao traineeDao;
    private TrainerDao trainerDao; // Needed for username filtering

    public TrainingServiceImpl(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
        logger.info("TrainingServiceImpl initialized.");
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) { this.traineeDao = traineeDao; }

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }

    // Training Service class should support possibility to create/select Training profile.
    @Override
    @PreAuthorize("isAuthenticated()")
    public Training createProfile(Training training) {
        logger.info("Creating training: {}", training.getTrainingName());
        return trainingDao.save(training);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Training> selectProfile(Long id) {
        logger.info("Attempting to select Training profile with ID: {}", id);
        Optional<Training> training = trainingDao.findById(id);

        if (training.isPresent()) {
            logger.debug("Training found: {}", training.get().getTrainingName());
        } else {
            logger.warn("Training not found for ID: {}", id);
        }

        return training;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Training> selectProfile(String trainingName){
        logger.info("Attempting to select Training profile with name: {}", trainingName);
        Optional<Training> training = trainingDao.findByName(trainingName);

        if (training.isPresent()) {
            logger.debug("Training found: {}", training.get().getTrainingName());
        } else {
            logger.warn("Training not found for name: {}", trainingName);
        }
        return training;
    }

    // 14. Get Trainee Trainings List by criteria
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Training> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainerName, String type) {
        return trainingDao.findAll().stream()
                .filter(t -> traineeDao.findById(t.getId())
                        .map(trainee -> trainee.getUsername().equals(username)).orElse(false))
                .filter(t -> (from == null || !t.getTrainingDate().isBefore(from)))
                .filter(t -> (to == null || !t.getTrainingDate().isAfter(to)))
                .filter(t -> (trainerName == null || trainerDao.findById(t.getId())
                        .map(tr -> tr.getFirstName().equalsIgnoreCase(trainerName)).orElse(false)))
                .filter(t -> (type == null || t.getTrainingType().getTrainingTypeName().equalsIgnoreCase(type)))
                .collect(Collectors.toList());
    }

    // 15. Get Trainer Trainings List by criteria
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<Training> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName) {
        return trainingDao.findAll().stream()
                .filter(t -> trainerDao.findById(t.getId())
                        .map(trainer -> trainer.getUsername().equals(username)).orElse(false))
                .filter(t -> (from == null || !t.getTrainingDate().isBefore(from)))
                .filter(t -> (to == null || !t.getTrainingDate().isAfter(to)))
                .filter(t -> (traineeName == null || traineeDao.findById(t.getId())
                        .map(tr -> tr.getFirstName().equalsIgnoreCase(traineeName)).orElse(false)))
                .collect(Collectors.toList());
    }
}
