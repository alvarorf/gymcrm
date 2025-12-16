package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.storage.Storage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository // A specialization of @Component
public class TraineeDaoImpl implements TraineeDao {
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
        }
        // The put() method in Java's Map interface adds a new key-value pair or
        // update the value if the key already exists: V put(K key, V value)
        storage.getTraineeStorageMap().put(trainee.getUserId(), trainee);
        return trainee;

    }

    @Override
    public Optional<Trainee> findById(Long id) {
        return Optional.ofNullable(storage.getTraineeStorageMap().get(id));
    }

    @Override
    public List<Trainee> findAll() {
        return new ArrayList<>(storage.getTraineeStorageMap().values());
    }

    @Override
    public void delete(Long id) {
        storage.getTraineeStorageMap().remove(id);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        return storage.getTraineeStorageMap().values().stream()
                .filter(t -> t.getUsername().equals(username))
                .findFirst();
    }









}
