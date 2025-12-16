package com.gymcrm.dao.interfaces;

import com.gymcrm.model.Training;

import java.util.List;
import java.util.Optional;

public interface TrainingDao {
    // Training Service class should support possibility to create (save)
    // select(findById, findAll),  Training profile.
    Training save(Training training);
    Optional<Training> findById(Long id);
    List<Training> findAll();

}
