package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeProfileResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TraineeUpdateRequest;
import com.gymcrm.mapper.TraineeMapper;
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
    private UsernameGenerator usernameGenerator;
    private PasswordGenerator passwordGenerator;
    private TrainerDao trainerDao;
    private TraineeMapper traineeMapper;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    // Constructor-based injection (only for core dependencies)
    public TraineeServiceImpl(TraineeDao traineeDao)
    {
        this.traineeDao = traineeDao;
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    // Setter-based injection for the non-core dependencies
    @Autowired
    public void setUsernameGenerator(UsernameGenerator usernameGenerator) { this.usernameGenerator = usernameGenerator; }

    @Autowired
    public void setPasswordGenerator(PasswordGenerator passwordGenerator) { this.passwordGenerator = passwordGenerator; }
    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) { this.trainerDao = trainerDao; }

    @Autowired
    public void setTraineeMapper(TraineeMapper traineeeMapper) { this.traineeMapper = traineeMapper; }

    @Override
    public RegistrationResponse createProfile(TraineeRegistrationRequest request) {
        Nomenclature.info(logger, Action.CREATE);

        // Map request to Entity
        Trainee trainee = traineeMapper.toEntity(request);

        // Generate credentials (business logic stays in service)
        trainee.setUsername(usernameGenerator.generateUsername(trainee.getFirstName(), trainee.getLastName()));
        trainee.setPassword(passwordGenerator.generatePassword());

        // Persist
        Trainee savedTrainee = traineeDao.save(trainee);

        Nomenclature.success(logger, Action.CREATE, savedTrainee.getUsername());

        // 4. Map Entity back to the specific Registration DTO
        return traineeMapper.toRegistrationResponse(savedTrainee);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TraineeProfileResponse updateProfile(TraineeUpdateRequest request) {
        // Fetch
        Trainee existing = traineeDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(request.getUsername().getClass()))); // TODO: Possible bug. Check Nomenclature class

        // Map update
        traineeMapper.updateEntityFromRequest(request, existing);

        // Save & return as DTO
        Trainee updated = traineeDao.save(existing);
        return traineeMapper.toProfileResponse(updated);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<TraineeProfileResponse> selectTraineeProfile(String username) {
        Nomenclature.info(logger, Action.FETCH, username);
        return traineeDao.findByUsername(username)
                .map(traineeMapper::toProfileResponse);
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

    @Override
    @PreAuthorize("isAuthenticated()")
    public void toggleActivation(String username) {
        traineeDao.findByUsername(username).ifPresent(trainee -> {
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
