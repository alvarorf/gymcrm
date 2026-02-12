package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.model.Trainee;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.core.util.Nomenclature.Action;

import java.util.List;
import java.util.Optional;

// Logging
import org.slf4j.Logger; // Simple Logging Facade for Java
import org.slf4j.LoggerFactory;

@Repository // A specialization of @Component
public class TraineeDaoImpl implements TraineeDao {
    private static final Logger logger = LoggerFactory.getLogger(TraineeDaoImpl.class);
    private final TraineeRepository traineeRepository;

    // Req4: DAO with storage bean should be inserted into services beans using auto wiring.
    public TraineeDaoImpl(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    @Override
    @Transactional
    // The function "save" becomes a unit of work, adhering to the ACID (Atomicity, Consistency, Isolation, Durability) principles
    // If any operation during save() fails, the entire transaction is rolled back to maintain data integrity
    public Trainee save(Trainee trainee) {
        // ID generation is handled by the database (Identity) upon save
        Trainee savedTrainee = traineeRepository.save(trainee);
        Nomenclature.success(logger, Action.CREATE, savedTrainee.getUserId());
        return savedTrainee;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Nomenclature.info(logger, Action.DELETE);
        traineeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(String username) {
        Nomenclature.info(logger, Action.DELETE, username);
        // Uses the custom method in TraineeRepository
        traineeRepository.deleteByUsername(username);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        Nomenclature.info(logger, Action.FETCH, username);
        return traineeRepository.findByUsername(username);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        Nomenclature.info(logger, Action.FETCH, id);
        return traineeRepository.findById(id);
    }

    @Override
    public List<Trainee> findAll() {
        Nomenclature.info(logger, Action.FETCH);
        return traineeRepository.findAll();
    }
}