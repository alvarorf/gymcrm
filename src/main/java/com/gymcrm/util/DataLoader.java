package com.gymcrm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.model.TrainingType;
import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.repositories.TrainerRepository;
import com.gymcrm.repositories.TrainingRepository;
import com.gymcrm.repositories.TrainingTypeRepository;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.InputStream;
import java.util.List;

/*
    Req3: Implement the ability to initialize storage with some prepared data from the file
     during the application start (use spring bean post-processing features).
     Path to the concrete file should be set using property placeholder and external property file.
*/

@Component
public class DataLoader {
    private static final Logger logger = LoggerFactory.getLogger(DataLoader.class);
    private final ObjectMapper objectMapper;
    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final TrainingRepository trainingRepository;
    private final TrainingTypeRepository trainingTypeRepository;

    @Autowired
    public DataLoader(TraineeRepository traineeRepository,
                      TrainerRepository trainerRepository,
                      TrainingRepository trainingRepository,
                      TrainingTypeRepository trainingTypeRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.trainingRepository = trainingRepository;
        this.trainingTypeRepository = trainingTypeRepository;
        this.objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        logger.info("DataLoader initialized with JPA Repositories.");
    }

    @Transactional
    public void loadInitialData(String dataPath) {
        try {
            logger.info("Attempting to load initial data from: {}", dataPath);
            ClassPathResource resource = new ClassPathResource(dataPath);

            try (InputStream inputStream = resource.getInputStream()) {
                DataWrapper data = objectMapper.readValue(inputStream, DataWrapper.class);

                // 1. Save training types first (constants)
                if (data.getTrainingTypes() != null) {
                    trainingTypeRepository.saveAll(data.getTrainingTypes());
                    logger.info("Saved Training Types");
                }

                // 2. Save trainers (they now have valid specialization IDs to point to)
                if (data.getTrainers() != null) {
                    trainerRepository.saveAll(data.getTrainers());
                    logger.info("Saved Trainers");
                }

                // 3. Save trainees
                if (data.getTrainees() != null) {
                    traineeRepository.saveAll(data.getTrainees());
                    logger.info("Saved Trainees");
                }

                // 4. Save Trainings (must happen last because they refer to Trainees/Trainers)
                if (data.getTrainings() != null) {
                    trainingRepository.saveAll(data.getTrainings());
                    logger.info("Successfully persisted {} Trainings.", data.getTrainings().size());
                }

            }
        } catch (Exception e) {
            logger.error("Failed to load initial data from path: {}. Error: {}", dataPath, e.getMessage());
        }
    }

    @Data
    private static class DataWrapper {
        private List<TrainingType> trainingTypes;
        private List<Trainer> trainers;
        private List<Trainee> trainees;
        private List<Training> trainings;
    }
}