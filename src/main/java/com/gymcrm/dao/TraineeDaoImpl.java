package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.model.Trainee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    @Autowired
    public TraineeDaoImpl(TraineeRepository traineeRepository) {
        this.traineeRepository = traineeRepository;
    }

    @Override
    @Transactional
    // The function "save" becomes a unit of work, adhering to the ACID (Atomicity, Consistency, Isolation, Durability) principles
    // If any operation during save() fails, the entire transaction is rolled back to maintain data integrity
    public Trainee save(Trainee trainee) {
        // ID generation is handled by the database (Identity) upon save
        Trainee savedTrainee = traineeRepository.save(trainee);
        logger.info("Trainee saved successfully with ID: {}", savedTrainee.getUserId());
        return savedTrainee;
    }

    @Override
    @Transactional
    public void delete(Long id) {
        logger.info("Deleting Trainee with ID: {}", id);
        traineeRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void delete(String username) {
        logger.info("Attempting to delete Trainee with username: {}", username);
        // Uses the custom method in TraineeRepository
        traineeRepository.deleteByUsername(username);
    }

    @Override
    public Optional<Trainee> findByUsername(String username) {
        logger.debug("Finding Trainee by username: {}", username);
        return traineeRepository.findByUsername(username);
    }

    @Override
    public Optional<Trainee> findById(Long id) {
        logger.debug("Finding Trainee by ID: {}", id);
        return traineeRepository.findById(id);
    }

    @Override
    public List<Trainee> findAll() {
        logger.debug("Retrieving all Trainees");
        return traineeRepository.findAll();
    }
}