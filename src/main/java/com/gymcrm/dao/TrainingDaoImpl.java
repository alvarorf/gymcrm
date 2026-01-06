package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.repositories.TrainingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Logging
import org.slf4j.Logger; // Simple Logging Facade for Java
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);
    private final TrainingRepository trainingRepository;

    // Req4: DAO with storage bean should be inserted into services beans using auto wiring.
    @Autowired
    public TrainingDaoImpl(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    @Transactional
    public Training save(Training training) {
        Training savedTraining = trainingRepository.save(training);
        logger.info("Training saved successfully with ID: {}", savedTraining.getId());
        return savedTraining;
    }
    @Override
    public Optional<Training> findById(Long id) {
        logger.debug("Finding Training by ID: {}", id);
        return trainingRepository.findById(id);
    }

    @Override
    public Optional<Training> findByName(String trainingName) {
        logger.debug("Finding Training by name: {}", trainingName);
        return trainingRepository.findByTrainingName(trainingName);
    }

    @Override
    public List<Training> findAll() {
        logger.debug("Retrieving all Trainings");
        return trainingRepository.findAll();
    }
}