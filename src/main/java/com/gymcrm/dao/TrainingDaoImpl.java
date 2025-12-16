package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TrainingDao;
import com.gymcrm.model.Training;
import com.gymcrm.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class TrainingDaoImpl implements TrainingDao {
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
        }
        // The put() method in Java's Map interface adds a new key-value pair or
        // update the value if the key already exists: V put(K key, V value)
        storage.getTrainingStorageMap().put(training.getId(), training);

        return training;
    }
    @Override
    public Optional<Training> findById(Long id) {
        return Optional.ofNullable(storage.getTrainingStorageMap().get(id));
    }

    @Override
    public List<Training> findAll() {
        return new ArrayList<>(storage.getTrainingStorageMap().values());
    }

}
