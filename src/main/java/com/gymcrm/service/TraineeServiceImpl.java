package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
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
    // Non-Core Dependencies. Must NOT be final, for injection via Setter
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    // Constructor-based injection (only for core dependencies)
    public TraineeServiceImpl(TraineeDao traineeDao)
    {
        this.traineeDao = traineeDao;
        logger.info("TraineeServiceImpl initialized with constructor injection for DAO.");
    }

    // Setter-based injection for the non-core dependencies
    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) {
        this.usernameGenerator = usernameGenerator;
    }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) {
        this.passwordGenerator = passwordGenerator;
    }

    @Override
    public boolean authenticate(String username, String password) {
        return traineeDao.findByUsername(username)
                .map(trainee -> trainee.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    public Trainee createProfile(Trainee trainee)
    {
        logger.info("Attempting to create new Trainee profile: {} {}", trainee.getFirstName(), trainee.getLastName());
        String username = usernameGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = passwordGenerator.generatePassword();

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
    public Optional<Trainee> selectProfile(String username) {
        logger.info("Selecting Trainee profile by username: {}", username);
        return traineeDao.findByUsername(username);
    }

    @Override
    public void deleteProfile(Long id)
    {
        logger.warn("Attempting to delete Trainee profile with ID: {}", id);
        traineeDao.delete(id);
    }

    @Override
    public void updatePassword(Long id, String newPassword) {
        traineeDao.findById(id).ifPresent(trainee -> {
            trainee.setPassword(newPassword);
            traineeDao.save(trainee);
            logger.info("Password updated for Trainee ID: {}", id);
        });
    }
}
