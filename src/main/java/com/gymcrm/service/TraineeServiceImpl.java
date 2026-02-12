package com.gymcrm.service;

import com.gymcrm.core.util.*;
import com.gymcrm.dao.interfaces.*;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TraineeMapper;
import com.gymcrm.model.*;
import com.gymcrm.service.interfaces.TraineeService;
import com.gymcrm.core.util.*;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.gymcrm.core.util.Nomenclature.Action;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.*;
import org.springframework.transaction.annotation.Transactional;

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
    private final TraineeMapper traineeMapper;
    @Setter private CredentialsGenerator credentialsGenerator;
    @Setter private TrainerDao trainerDao;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    // Constructor-based injection (only for core dependencies)
    public TraineeServiceImpl(TraineeDao traineeDao, TraineeMapper traineeMapper, CredentialsGenerator credentialsGenerator, TrainerDao trainerDao)
    {
        this.traineeDao = traineeDao;
        this.traineeMapper = traineeMapper;
        this.credentialsGenerator = credentialsGenerator;
        this.trainerDao = trainerDao;
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    @Override
    @Transactional
    public RegistrationResponse createProfile(TraineeRegistrationRequest request) {
        Nomenclature.info(logger, Action.CREATE);

        // Map request to Entity
        Trainee trainee = traineeMapper.toEntity(request);

        // Generate credentials (business logic stays in service)
        GeneratedCredentialsResponse credentials = credentialsGenerator.generate(
                trainee.getFirstName(),
                trainee.getLastName()
        );

        // Set the data on the entity
        trainee.setUsername(credentials.username());
        trainee.setPassword(credentials.encodedPassword());

        // Persist
        Trainee savedTrainee = traineeDao.save(trainee);

        Nomenclature.success(logger, Action.CREATE, savedTrainee.getUsername());

        // Return response with the RAW password so the user can log in
        return new RegistrationResponse(credentials.username(), credentials.rawPassword());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public TraineeProfileResponse updateProfile(TraineeUpdateRequest request) {
        // Fetch
        Trainee existing = traineeDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(request.getUsername())));

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
    public Optional<TraineeProfileResponse> selectProfile(Long id) {
        Nomenclature.info(logger, Action.FETCH, id);
        return traineeDao.findById(id)
                .map(traineeMapper::toProfileResponse);
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
