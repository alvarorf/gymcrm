package com.gymcrm.service.interfaces;

/*
Training Service class should support possibility to create/select Training profile.
 */

import com.gymcrm.model.Training;
import java.util.Optional;

public interface TrainingService {
    Training createProfile(Training training);
    Optional<Training> selectProfile(Long id);
}
