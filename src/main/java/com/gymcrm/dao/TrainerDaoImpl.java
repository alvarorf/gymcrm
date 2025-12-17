package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
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
public class TrainerDaoImpl implements TrainerDao {
    private final Storage storage;

    // Req4: DAO with storage bean should be inserted into services beans using auto wiring.
    @Autowired
    public TrainerDaoImpl(Storage storage) {
        this.storage = storage;
    }

    @Override
    public Trainer save(Trainer trainer) {
        if (trainer.getUserId() == null) {
            // If the trainer does not exist, we can generate a new user id for them
            trainer.setUserId(storage.getNextTrainerId());
        }
        // The put() method in Java's Map interface adds a new key-value pair or
        // update the value if the key already exists: V put(K key, V value)
        storage.getTrainerStorageMap().put(trainer.getUserId(), trainer);

        return trainer;
    }
    @Override
    public Optional<Trainer> findById(Long id) {
        return Optional.ofNullable(storage.getTrainerStorageMap().get(id));
    }

    @Override
    public List<Trainer> findAll() {
        return new ArrayList<>(storage.getTrainerStorageMap().values());
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        return storage.getTrainerStorageMap().values().stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst();
    }
}
