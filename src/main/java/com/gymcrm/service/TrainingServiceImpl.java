package com.gymcrm.service;

import com.gymcrm.dao.interfaces.*;
import com.gymcrm.dto.*;
import com.gymcrm.mapper.TrainingMapper;
import com.gymcrm.model.*;
import com.gymcrm.service.interfaces.TrainingService;
import lombok.Setter;
import org.slf4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import com.gymcrm.util.Nomenclature;
import com.gymcrm.util.Nomenclature.Action;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service  // Could also be @Component
public class TrainingServiceImpl implements TrainingService {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    // Core Dependency: DAO, injected via constructor
    private final TrainingDao trainingDao;

    // Non-core dependencies (needed for username filtering), injected via setters
    @Setter private TraineeDao traineeDao;
    @Setter private TrainerDao trainerDao;
    @Setter private TrainingMapper trainingMapper;

    public TrainingServiceImpl(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    // Full constructor injection (for testing)
    @Autowired
    public TrainingServiceImpl(TrainingDao trainingDao, TraineeDao traineeDao, TrainerDao trainerDao) {
        this.trainingDao = trainingDao;
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        Nomenclature.info(logger, Action.INITIALIZE);
    }

    // Training Service class should support possibility to create/select Training profile.
    @Override
    @PreAuthorize("isAuthenticated()")
    public void createProfile(TrainingCreateRequest request) {
        Nomenclature.info(logger, Action.CREATE, request.getTrainingName());

        // Logic moved from Controller to Service
        Trainee trainee = traineeDao.findByUsername(request.getTraineeUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(request.getTraineeUsername())));

        Trainer trainer = trainerDao.findByUsername(request.getTrainerUsername())
                .orElseThrow(() -> new RuntimeException(Nomenclature.getNotFoundMsg(request.getTrainerUsername())));

        Training training = trainingMapper.toEntity(trainee, trainer, request);
        trainingDao.save(training);

        Nomenclature.success(logger, Action.CREATE, request.getTrainingName());
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Training> selectProfile(Long id) {
        Nomenclature.info(logger, Action.FETCH, id);
        Optional<Training> training = trainingDao.findById(id);

        training.ifPresentOrElse(
                t -> Nomenclature.success(logger, Action.FETCH, t.getTrainingName()),
                () -> Nomenclature.warn(logger, Action.FETCH, id)
        );

        return training;
    }

    @Override
    @PreAuthorize("isAuthenticated()")
    public Optional<Training> selectProfile(String trainingName){
        Nomenclature.info(logger, Action.FETCH, trainingName);
        Optional<Training> training = trainingDao.findByName(trainingName);

        training.ifPresentOrElse(
                t -> Nomenclature.success(logger, Action.FETCH, t.getTrainingName()),
                () -> Nomenclature.warn(logger, Action.FETCH, trainingName)
        );

        return training;
    }

    // 14. Get Trainee Trainings List by criteria
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<TraineeTrainingResponse> getTraineeTrainings(String username, LocalDate from, LocalDate to, String trainerName, String type) {
        return trainingDao.findAll().stream()
                // Access entity relationships directly instead of searching IDs manually
                .filter(t -> t.getTrainee().getUsername().equals(username))
                .filter(t -> (from == null || !t.getTrainingDate().isBefore(from)))
                .filter(t -> (to == null || !t.getTrainingDate().isAfter(to)))
                .filter(t -> (trainerName == null || t.getTrainer().getFirstName().equalsIgnoreCase(trainerName)))
                .filter(t -> (type == null || t.getTrainingType().getTrainingTypeName().equalsIgnoreCase(type)))
                .map(trainingMapper::toTraineeTrainingResponse)
                .collect(Collectors.toList());
    }

    // 15. Get Trainer Trainings List by criteria
    @Override
    @PreAuthorize("isAuthenticated()")
    public List<TrainerTrainingResponse> getTrainerTrainings(String username, LocalDate from, LocalDate to, String traineeName) {
        return trainingDao.findAll().stream()
                .filter(t -> t.getTrainer().getUsername().equals(username))
                .filter(t -> (from == null || !t.getTrainingDate().isBefore(from)))
                .filter(t -> (to == null || !t.getTrainingDate().isAfter(to)))
                .filter(t -> (traineeName == null || t.getTrainee().getFirstName().equalsIgnoreCase(traineeName)))
                .map(trainingMapper::toTrainerTrainingResponse)
                .collect(Collectors.toList());
    }
}
