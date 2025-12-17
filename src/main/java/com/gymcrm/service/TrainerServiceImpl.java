package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.util.UsernamePasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service  // Could also be @Component
public class TrainerServiceImpl implements TrainerService {

    // Dependency injected via constructor (because, by req4:
    // "DAO with storage bean should be inserted into services beans using auto wiring")
    private final TrainerDao trainerDao;

    // Dependency injected via Setter (UsernamePasswordGenerator because, by req4:
    // "The rest of the injections should be done in a setter-based way")
    private UsernamePasswordGenerator generator; // Must not be final for setter injection to work

    // Constructor-based injection, we only inject TrainerDao because it is a core dependency

    public TrainerServiceImpl(TrainerDao trainerDao, UsernamePasswordGenerator generator)
    {
        this.trainerDao = trainerDao;
    }

    // Setter-based injection for UsernamePasswordGenerator
    @Autowired
    public void setGenerator(UsernamePasswordGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Trainer createProfile(Trainer trainer)
    {
        String username = generator.generateUsername(trainer.getFirstName(), trainer.getLastName());
        String password = generator.generatePassword();


        trainer.setUsername(username);
        trainer.setPassword(password);

        trainer.setActive(true);

        return trainerDao.save(trainer);
    }

    @Override
    public Trainer updateProfile(Trainer trainer)
    {
        return trainerDao.save(trainer);
    }

    @Override
    public Optional<Trainer> selectProfile(Long id)
    {
        return trainerDao.findById(id);
    }
}
