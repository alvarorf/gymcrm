package com.gymcrm.repositories;

import com.gymcrm.model.TrainingType;
import lombok.NonNull;
import org.springframework.data.repository.CrudRepository;

import java.util.*;

public interface TrainingTypeRepository extends CrudRepository<@NonNull TrainingType, @NonNull Long> {

    @Override
    @NonNull
    List<TrainingType> findAll();
    Optional<TrainingType> findByTrainingTypeName(String name);
}
