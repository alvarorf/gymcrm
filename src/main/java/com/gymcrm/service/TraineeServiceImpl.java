package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.util.UsernameGenerator;
import com.gymcrm.util.PasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.gymcrm.util.Nomenclature;
import com.gymcrm.util.Nomenclature.Action;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

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
    private UsernameGenerator usernameGenerator;  // TODO: We can have a credentials generator for both (generalize)
    private PasswordGenerator passwordGenerator;
    private TrainerDao trainerDao;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    // Constructor-based injection (only for core dependencies)
    public TraineeServiceImpl(TraineeDao traineeDao)
    {
        this.traineeDao = traineeDao;
        // Output: [CONSTRUCTOR] Context initialized for Trainee
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    // Setter-based injection for the non-core dependencies
    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) { this.usernameGenerator = usernameGenerator; }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) { this.passwordGenerator = passwordGenerator; }
    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }

    @Override
    public Trainee createProfile(Trainee trainee) { // TODO: Split up into at least two methods OR perhaps use the credentials generator service to generate the password, username, set them and return the model
        Nomenclature.info(logger, Action.CREATE);
        String username = usernameGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName());
        String password = passwordGenerator.generatePassword();

        trainee.setUsername(username);
        trainee.setPassword(password);

        Trainee savedTrainee = traineeDao.save(trainee);
        Nomenclature.success(logger, Action.CREATE, savedTrainee.getUsername());
        return savedTrainee;
    }

    @Override
    public boolean authenticate(String username, String password) {
        Nomenclature.info(logger, Action.AUTH);
        return traineeDao.findByUsername(username)
                .map(trainee -> trainee.getPassword().equals(password))
                .orElse(false);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Trainee updateProfile(Trainee trainee) {
        // Output: [updateProfile] Attempting to update Trainee
        Nomenclature.info(logger, Action.UPDATE);
        if (trainee.getFirstName() == null || trainee.getLastName() == null) {
            throw new IllegalArgumentException(Nomenclature.MSG_REQUIRED);
        }
        return traineeDao.save(trainee);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Trainee> selectTraineeProfile(String targetUser) {
        Nomenclature.info(logger, Action.FETCH, targetUser); // Auto: [selectProfile] Attempting to retrieve context for jane.doe Trainee
        return traineeDao.findByUsername(targetUser);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Trainee> selectProfile(Long id) {
        Nomenclature.info(logger, Action.FETCH, id);
        return traineeDao.findById(id);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteProfile(Long id) {
        Nomenclature.info(logger, Action.DELETE, id);
        traineeDao.delete(id);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void deleteProfile(String targetUser) {
        Nomenclature.info(logger, Action.DELETE, targetUser);
        // 13: Delete by username
        traineeDao.findByUsername(targetUser).ifPresent(t -> traineeDao.delete(t.getUserId()));
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(Long id, String newPassword) {
        traineeDao.findById(id).ifPresent(trainee -> {
            trainee.setPassword(newPassword);
            traineeDao.save(trainee);
            Nomenclature.success(logger, Action.UPDATE_SENSITIVE, "ID: " + id);
        });
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void toggleActivation(Long id) {
        traineeDao.findById(id).ifPresent(trainee -> {
            trainee.setActive(!trainee.isActive());
            traineeDao.save(trainee);
            Nomenclature.info(logger, Action.TOGGLE);
        });
    }

    // 18. Update Trainee's trainers list
    @Override
    @PreAuthorize("isAuthenticated()")
    public void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(Trainee.class)));

        Set<Trainer> newTrainers = trainerUsernames.stream()
                .map(u -> trainerDao.findByUsername(u).orElse(null))
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        trainee.setTrainers(newTrainers);
        traineeDao.save(trainee);
        Nomenclature.success(logger, Action.UPDATE, traineeUsername);
    }
}
