package com.gymcrm.storage;

import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;

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

    @Autowired
    public Storage(@Qualifier("trainerMap") Map<Long, Trainer> trainerStorageMap,
                   @Qualifier("trainingMap") Map<Long, Training> trainingStorageMap) {
    }
}
