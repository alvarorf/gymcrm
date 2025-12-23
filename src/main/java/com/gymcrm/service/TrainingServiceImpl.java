package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.service.interfaces.TrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service  // Could also be @Component
public class TrainingServiceImpl implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    // Core Dependency: DAO, injected via constructor (autowired)
    private final TrainingDao trainingDao;

    public TrainingServiceImpl(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
        logger.info("TrainingServiceImpl initialized.");
    }

    // Training Service class should support possibility to create/select Training profile.

    @Override
    public Training createProfile(Training training) {
        logger.info("Attempting to create new Training profile: {}", training.getTrainingName());
        Training savedTraining = trainingDao.save(training);
        logger.info("Training created successfully with ID: {}", savedTraining.getId());
        return savedTraining;
    }

    @Override
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
}
