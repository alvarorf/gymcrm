package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Logging
import org.slf4j.Logger; // Simple Logging Facade for Java
import org.slf4j.LoggerFactory;

@Repository // A specialization of @Component
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private final Storage storage;

    // Req4: DAO with storage bean should be inserted into services beans using auto wiring.
    @Autowired
    public TraineeDaoImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Trainee save(Trainee trainee) {
        if (trainee.getUserId() == null) {
            // If the trainee does not exist, we can generate a new user id for them
            trainee.setUserId(storage.getNextTraineeId());
            logger.debug("Generating new ID {} for Trainee", trainee.getUserId());
        }
        // The put() method in Java's Map interface adds a new key-value pair or
        // update the value if the key already exists: V put(K key, V value)
        storage.getTraineeStorageMap().put(trainee.getUserId(), trainee);
        logger.info("Trainee saved successfully with ID: {}", trainee.getUserId());

        return trainee;
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        logger.debug("Finding Trainee by ID: {}", id);
        return Optional.ofNullable(storage.getTraineeStorageMap().get(id));
    }

    @Override
    public List<Trainee> findAll() {
        logger.debug("Retrieving all Trainees");
        return new ArrayList<>(storage.getTraineeStorageMap().values());
    }

    @Override
    public void delete(Long id) {
        logger.info("Deleting Trainee with ID: {}", id);
        storage.getTraineeStorageMap().remove(id);
    }

    @Override
    public void delete(String username) {
        Optional<Trainee> trainee = findByUsername(username);
        // If present, get the ID and remove from map
        if (trainee.isPresent()) {
            Long id = trainee.get().getUserId();
            storage.getTraineeStorageMap().remove(id);
            logger.info("Trainee with username {} and ID {} deleted successfully", username, id);
        } else {
            logger.warn("Delete failed: No trainee found with username {}", username);
        }
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        logger.debug("Finding Trainee by username: {}", username);
        return storage.getTraineeStorageMap().values().stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst();
    }
}