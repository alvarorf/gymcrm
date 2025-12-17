package com.gymcrm.storage;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;

import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
// We need Qualifier to distinguish between beans of the same type when Spring performs DI
// Because in AppConfig.java, we have three separate beans, of the same type: Map<Long,?>
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

// To load the initial data
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

@Component
public class Storage {
    private static final Logger logger = LoggerFactory.getLogger(Storage.class);

    // Requirement 2: Common in-memory storage - java map.
    // Each entity stored under a separate namespace.
    @Getter  private final Map<Long, Trainee> traineeStorageMap;
    @Getter  private final Map<Long, Trainer> trainerStorageMap;
    @Getter  private final Map<Long, Training> trainingStorageMap;

    // We inject the separate Map beans here
    @Autowired
    public Storage(@Qualifier("traineeMap") Map<Long, Trainee> traineeStorageMap,
                   @Qualifier("trainerMap") Map<Long, Trainer> trainerStorageMap,
                   @Qualifier("trainingMap") Map<Long, Training> trainingStorageMap) {
        this.traineeStorageMap = traineeStorageMap;
        this.trainerStorageMap = trainerStorageMap;
        this.trainingStorageMap = trainingStorageMap;
    }

    // Initial ID generator
    private long traineeIdCounter = 1;
    private long trainerIdCounter = 1;
    private long trainingIdCounter = 1;

    // Helpers to generate ID for new entities
    public Long getNextTraineeId() {
        return traineeIdCounter++;
    }

    public Long getNextTrainerId() {
        return trainerIdCounter++;
    }

    public Long getNextTrainingId() {
        return trainingIdCounter++;
    }


    /*
    Req3: Implement the ability to initialize storage with some prepared data from the file
     during the application start (use spring bean post-processing features).
     Path to the concrete file should be set using property placeholder and external property file.
     */
    public void loadInitialData(String dataPath) {
        // TODO: Implement logic to read the file (e.g., JSON) at 'dataPath'
        // and populate the storage maps. This logic may be executed
        // by a BeanPostProcessor or InitializingBean, according to req3.
        logger.info("Attempting to load initial data from: {}", dataPath);
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.registerModule(new JavaTimeModule()); // Required for LocalDate support

            // Read from classpath if the path starts with classpath: or is a relative resource
            InputStream is = new ClassPathResource(dataPath).getInputStream();
            DataWrapper data = mapper.readValue(is, DataWrapper.class);

            // Populate Maps
            data.trainees.forEach(t -> traineeStorageMap.put(t.getUserId(), t));
            data.trainers.forEach(t -> trainerStorageMap.put(t.getUserId(), t));
            data.trainings.forEach(t -> trainingStorageMap.put(t.getId(), t));

            // Sync counters to avoid ID collisions with imported data
            traineeIdCounter = traineeStorageMap.keySet().stream().max(Long::compare).orElse(0L) + 1;
            trainerIdCounter = trainerStorageMap.keySet().stream().max(Long::compare).orElse(0L) + 1;
            trainingIdCounter = trainingStorageMap.keySet().stream().max(Long::compare).orElse(0L) + 1;

            logger.info("Successfully initialized storage with {} trainees, {} trainers, and {} trainings.",
                    traineeStorageMap.size(), trainerStorageMap.size(), trainingStorageMap.size());

        } catch (Exception e) {
            logger.error("Failed to load initial data from path: {}", dataPath, e);
        }
    }

    // Helper class for Jackson mapping
    public static class DataWrapper {
        public List<Trainee> trainees;
        public List<Trainer> trainers;
        public List<Training> trainings;
    }
}
