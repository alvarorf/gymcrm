package com.gymcrm.dao;

import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.repositories.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

// Logging
import org.slf4j.Logger; // Simple Logging Facade for Java
import org.slf4j.LoggerFactory;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);
    private final TrainerRepository trainerRepository;

    @Autowired
    public TrainerDaoImpl(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    @Transactional
    public Trainer save(Trainer trainer) {
        // ID generation is handled by the database (Identity) upon save
        Trainer savedTrainer = trainerRepository.save(trainer);
        logger.info("Trainer saved successfully with ID: {}", savedTrainer.getUserId());
        return savedTrainer;
    }
    @Override
    public Optional<Trainer> findById(Long id) {
        logger.debug("Finding Trainer by ID: {}", id);
        return trainerRepository.findById(id);
    }

    @Override
    public List<Trainer> findAll() {
        logger.debug("Retrieving all Trainers");
        return trainerRepository.findAll();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        logger.debug("Finding Trainer by username: {}", username);
        return trainerRepository.findByUsername(username);
    }
}