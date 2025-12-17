package com.gymcrm.storage;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
// We need Qualifier to distinguish between beans of the same type when Spring performs DI
// Because in AppConfig.java, we have three separate beans, of the same type: Map<Long,?>
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Storage {
    // Requirement 2: Common in-memory storage - java map.
    // Each entity stored under a separate namespace.
    @Getter
    private final Map<Long, Trainee> traineeStorageMap;
    @Getter
    private final Map<Long, Trainer> trainerStorageMap;
    @Getter
    private final Map<Long, Training> trainingStorageMap;

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
        System.out.println("--- Storage initialized with data from: " + dataPath.toLowerCase() + " ---");
    }


}
