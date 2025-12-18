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
import org.springframework.stereotype.Component;

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
}
