package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Logging
import org.slf4j.Logger; // Simple Logging Facade for Java
import org.slf4j.LoggerFactory;

@Repository
public class TrainingDaoImpl implements TrainingDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainingDaoImpl.class);
    private final Storage storage;

    // Req4: DAO with storage bean should be inserted into services beans using auto wiring.
    @Autowired
    public TrainingDaoImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Training save(Training training) {
        if (training.getId() == null) {
            // If the training does not exist, we can generate a new user id for them
            training.setId(storage.getNextTrainingId());
            logger.debug("Generating new ID {} for Training", training.getId());
        }
        // The put() method in Java's Map interface adds a new key-value pair or
        // update the value if the key already exists: V put(K key, V value)
        storage.getTrainingStorageMap().put(training.getId(), training);
        logger.info("Training saved successfully with ID: {}", training.getId());

        return training;
    }
    @Override
    public Optional<Training> findById(Long id) {
        logger.debug("Finding Training by ID: {}", id);
        return Optional.ofNullable(storage.getTrainingStorageMap().get(id));
    }

    @Override
    public List<Training> findAll() {
        logger.debug("Retrieving all Trainings");
        return new ArrayList<>(storage.getTrainingStorageMap().values());
    }
}