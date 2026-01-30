package com.gymcrm.service;

import com.gymcrm.dao.interfaces.*;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TrainerMapper;
import com.gymcrm.model.*;
import com.gymcrm.service.interfaces.*;
import com.gymcrm.util.*;
import lombok.Setter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.gymcrm.util.Nomenclature;
import com.gymcrm.util.Nomenclature.Action;

import java.util.*;
import java.util.stream.Collectors;

import org.slf4j.*;
import org.springframework.transaction.annotation.Transactional;

@Service  // Could also be @Component
public class TrainerServiceImpl implements TrainerService {

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    // Dependency injected via constructor (because, by req4:
    // "DAO with storage bean should be inserted into services beans using auto wiring")
    private final TrainerDao trainerDao;
    private final TrainerMapper trainerMapper;

    // Non-Core Dependencies. Must NOT be final, for injection via Setter
    @Setter private UsernameGenerator usernameGenerator;
    @Setter private PasswordGenerator passwordGenerator;
    @Setter private TraineeDao traineeDao;
    @Setter private TrainingTypeService trainingTypeService;

    // Constructor-based injection, we only inject core dependencies
    public TrainerServiceImpl(TrainerDao trainerDao, TrainerMapper trainerMapper)
    {
        this.trainerDao = trainerDao;
        this.trainerMapper = trainerMapper;
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    @Override
    @Transactional
    public RegistrationResponse createProfile(TrainerRegistrationRequest request)
    {
        // Business logic: find specialization
        TrainingType specialization = trainingTypeService.findByName(request.getSpecialization().getTrainingTypeName())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(request.getSpecialization().getTrainingTypeName())));

        // Mapping
        Trainer trainer = trainerMapper.toEntity(request, specialization);
        trainer.setUsername(usernameGenerator.generateUsername(trainer.getFirstName(), trainer.getLastName()));
        trainer.setPassword(passwordGenerator.generatePassword());

        Trainer saved = trainerDao.save(trainer);
        Nomenclature.success(logger, Action.CREATE, saved.getUsername());

        return trainerMapper.toRegistrationResponse(saved);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    @Transactional
    public TrainerProfileResponse updateProfile(TrainerUpdateRequest request) {
        Nomenclature.info(logger, Action.UPDATE, request.getUsername());

        Trainer existing = trainerDao.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(request.getUsername())));

        trainerMapper.updateEntityFromRequest(request, existing);
        Trainer updated = trainerDao.save(existing);

        return trainerMapper.toProfileResponse(updated);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<TrainerProfileResponse> selectTrainerProfile(Long id)
    {
        Nomenclature.info(logger, Action.FETCH, id);
        return trainerDao.findById(id)
                .map(trainerMapper::toProfileResponse);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<TrainerProfileResponse> selectTrainerProfile(String username) {

        Nomenclature.info(logger, Action.FETCH, username);
        return trainerDao.findByUsername(username)
                .map(trainerMapper::toProfileResponse);
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void updatePassword(Long id, String newPassword) {
        trainerDao.findById(id).ifPresent(trainer -> {
            trainer.setPassword(newPassword);
            trainerDao.save(trainer);
            Nomenclature.success(logger, Action.UPDATE_SENSITIVE, "ID: " + id);
        });
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void toggleActivation(Long id) {
        trainerDao.findById(id).ifPresent(trainer -> {
            trainer.setActive(!trainer.isActive());
            trainerDao.save(trainer);
            Nomenclature.info(logger, Action.TOGGLE, trainer.isActive());
        });
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public void toggleActivation(String username) {
        trainerDao.findByUsername(username).ifPresent(trainer -> {
            trainer.setActive(!trainer.isActive());
            trainerDao.save(trainer);
            Nomenclature.info(logger, Action.TOGGLE);
        });
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public List<TrainerShortResponse> getUnassignedActiveTrainersByTraineeUsername(String traineeUsername) {
        Trainee trainee = traineeDao.findByUsername(traineeUsername)
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(traineeUsername)));

        // Fetch all trainers, filter for Active AND Not in Trainee's current list
        return trainerDao.findAll().stream()
                .filter(Trainer::isActive) // Requirement 10: "active trainers"
                .filter(trainer -> trainee.getTrainers() == null || !trainee.getTrainers().contains(trainer))
                .map(trainerMapper::toShortResponse)
                .collect(Collectors.toList());
    }
}
