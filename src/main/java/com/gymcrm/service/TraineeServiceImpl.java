package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.util.UsernamePasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
Trainee Service class should support possibility to create/update/delete/select Trainee
profile.
*/


/*
From: https://docs.spring.io/spring-framework/docs/current/javadoc-api/org/springframework/stereotype/Service.html
@Service:
Indicates that an annotated class is a "Service", originally defined by Domain-Driven Design (Evans, 2003)
as "an operation offered as an interface that stands alone in the model, with no encapsulated state."
It is a specialization (implementation) of @Component and allows TraineeServiceImpl to be autodetected through classpath scanning.
 */
@Service
public class TraineeServiceImpl implements TraineeService {
    // Why final? Because TraineeDao is a core dependency, injected via the constructor
    private final TraineeDao traineeDao;
    // Non-Core Dependency (UsernamePasswordGenerator). Must NOT be final, for injection via Setter
    private UsernamePasswordGenerator generator;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    // Constructor-based injection (only for core dependencies)
    public TraineeServiceImpl(TraineeDao traineeDao)
    {
        this.traineeDao = traineeDao;
        logger.info("TraineeServiceImpl initialized with constructor injection for DAO.");
    }

    // Setter-based injection for the non-core dependency (UsernamePasswordGenerator)
    @Autowired
    public void setGenerator(UsernamePasswordGenerator generator) {
        this.generator = generator;
    }

    @Override
    public Trainee createProfile(Trainee trainee)
    {
        logger.info("Attempting to create new Trainee profile: {} {}", trainee.getFirstName(), trainee.getLastName());
        String username = generator.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = generator.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(password);

        Trainee savedTrainee = traineeDao.save(trainee);
        logger.info("Trainee created successfully. Username: {}", savedTrainee.getUsername());
        return savedTrainee;
    }

    @Override
    public Trainee updateProfile(Trainee trainee)
    {
        logger.info("Attempting to update Trainee profile with ID: {}", trainee.getUserId());
        return traineeDao.save(trainee);
    }

    @Override
    public Optional<Trainee> selectProfile(Long id)
    {
        logger.info("Attempting to select Trainee profile with ID: {}", id);
        return traineeDao.findById(id);
    }

    @Override
    public void deleteProfile(Long id)
    {
        logger.warn("Attempting to delete Trainee profile with ID: {}", id);
        traineeDao.delete(id);
    }
}
