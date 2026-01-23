package com.gymcrm.service.interfaces;

import com.gymcrm.model.TrainingType;
import java.util.List;
import java.util.Optional;

public interface TrainingTypeService {
    List<TrainingType> getAllTrainingTypes();
    Optional<TrainingType> findByName(String name);
}