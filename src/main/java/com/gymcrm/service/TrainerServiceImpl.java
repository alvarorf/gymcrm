package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service  // Could also be @Component
public class TrainerServiceImpl implements TrainerService {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    // Dependency injected via constructor (because, by req4:
    // "DAO with storage bean should be inserted into services beans using auto wiring")
    private final TrainerDao trainerDao;

    // Non-Core Dependencies. Must NOT be final, for injection via Setter
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator; // Must not be final for setter injection to work

    // Constructor-based injection, we only inject TrainerDao because it is a core dependency

    public TrainerServiceImpl(TrainerDao trainerDao)
    {
        this.trainerDao = trainerDao;
        logger.info("TrainerServiceImpl initialized with constructor injection for DAO.");
    }

    // Setter-based injection for generators
    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public Trainer createProfile(Trainer trainer)
    {
        logger.info("Attempting to create new Trainer profile: {} {}", trainer.getFirstName(), trainer.getLastName());
        String username = usernameGenerator.generateUsername(trainer.getFirstName(), trainer.getLastName());
        String password = passwordGenerator.generatePassword();

        trainer.setUsername(username);
        trainer.setPassword(password);

        Trainer savedTrainer = trainerDao.save(trainer);
        logger.info("Trainer created successfully. Username: {}", savedTrainer.getUsername());
        return savedTrainer;
    }

    @Override
    public Trainer updateProfile(Trainer trainer)
    {
        logger.info("Attempting to update Trainer profile with ID: {}", trainer.getUserId());
        return trainerDao.save(trainer);
    }

    @Override
    public Optional<Trainer> selectProfile(Long id)
    {
        logger.info("Attempting to select Trainer profile with ID: {}", id);
        return trainerDao.findById(id);
    }
}
