package com.gymcrm.dao;

import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.core.util.Nomenclature.Action;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.repositories.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public class TrainerDaoImpl implements TrainerDao {
    private static final Logger logger = LoggerFactory.getLogger(TrainerDaoImpl.class);
    private final TrainerRepository trainerRepository;

    public TrainerDaoImpl(TrainerRepository trainerRepository) {
        this.trainerRepository = trainerRepository;
    }

    @Override
    @Transactional
    public Trainer save(Trainer trainer) {
        // ID generation is handled by the database (Identity) upon save
        Trainer savedTrainer = trainerRepository.save(trainer);
        Nomenclature.success(logger, Action.CREATE, savedTrainer.getUserId());
        return savedTrainer;
    }
    @Override
    public Optional<Trainer> findById(Long id) {
        Nomenclature.info(logger, Action.FETCH, id);
        return trainerRepository.findById(id);
    }

    @Override
    public List<Trainer> findAll() {
        Nomenclature.info(logger, Action.FETCH);
        return trainerRepository.findAll();
    }

    @Override
    public Optional<Trainer> findByUsername(String username) {
        Nomenclature.info(logger, Action.FETCH, username);
        return trainerRepository.findByUsername(username);
    }
}