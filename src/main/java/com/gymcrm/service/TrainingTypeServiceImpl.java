package com.gymcrm.service;

import com.gymcrm.model.TrainingType;
import com.gymcrm.repositories.TrainingTypeRepository;
import com.gymcrm.service.interfaces.TrainingTypeService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TrainingTypeServiceImpl implements TrainingTypeService {

    private final TrainingTypeRepository trainingTypeRepository;

    public TrainingTypeServiceImpl(TrainingTypeRepository trainingTypeRepository) {
        this.trainingTypeRepository = trainingTypeRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TrainingType> getAllTrainingTypes() {
        return trainingTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<TrainingType> findByName(String name) {
        return trainingTypeRepository.findByTrainingTypeName(name);
    }
}