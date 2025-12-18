package com.gymcrm.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;
import com.gymcrm.storage.Storage;
import lombok.Data;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

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

    public DataLoader() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public void loadInitialData(Storage storage, String dataPath) {
        try {
            logger.info("Attempting to load initial data from: {}", dataPath);
            ClassPathResource resource = new ClassPathResource(dataPath);

            try (InputStream inputStream = resource.getInputStream()) {
                DataWrapper data = objectMapper.readValue(inputStream, DataWrapper.class);

                if (data.getTrainers() != null) {
                    data.getTrainers().forEach(t -> storage.getTrainerStorageMap().put(t.getUserId(), t));
                }
                if (data.getTrainees() != null) {
                    data.getTrainees().forEach(t -> storage.getTraineeStorageMap().put(t.getUserId(), t));
                }
                if (data.getTrainings() != null) {
                    data.getTrainings().forEach(t -> storage.getTrainingStorageMap().put(t.getId(), t));
                }
                logger.info("Successfully loaded initial data into storage.");
            }
        } catch (Exception e) {
            logger.error("Failed to load initial data from path: {}", dataPath, e);
        }
    }

    @Data
    private static class DataWrapper {
        private List<Trainer> trainers;
        private List<Trainee> trainees;
        private List<Training> trainings;
    }
}