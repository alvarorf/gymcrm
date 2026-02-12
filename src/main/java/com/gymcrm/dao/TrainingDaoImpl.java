package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.repositories.TrainingRepository;
import com.gymcrm.core.util.Nomenclature;
import org.springframework.stereotype.Repository;

import java.util.*;

// Logging
import org.slf4j.*; // Simple Logging Facade for Java
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);
    private final TrainingRepository trainingRepository;

    public TrainingDaoImpl(TrainingRepository trainingRepository) {
        this.trainingRepository = trainingRepository;
    }

    @Override
    @Transactional
    public Training save(Training training) {
        Training savedTraining = trainingRepository.save(training);
        Nomenclature.success(logger, Nomenclature.Action.CREATE, savedTraining.getId());
        return savedTraining;
    }
    @Override
    public Optional<Training> findById(Long id) {
        Nomenclature.info(logger, Nomenclature.Action.FETCH, id);
        return trainingRepository.findById(id);
    }

    @Override
    public Optional<Training> findByName(String trainingName) {
        Nomenclature.info(logger, Nomenclature.Action.FETCH, trainingName);
        return trainingRepository.findByTrainingName(trainingName);
    }

    @Override
    public List<Training> findAll() {
        Nomenclature.info(logger, Nomenclature.Action.FETCH);
        return trainingRepository.findAll();
    }
}