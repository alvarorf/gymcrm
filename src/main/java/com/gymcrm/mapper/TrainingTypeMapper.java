package com.gymcrm.mapper;

import com.gymcrm.dto.TrainingTypeRequest;
import com.gymcrm.dto.TrainingTypeResponse;
import com.gymcrm.model.TrainingType;
import org.springframework.stereotype.Component;

@Component
public class TrainingTypeMapper {

    // Used for test data arrangement and registration logic
    public TrainingType toEntity(TrainingTypeRequest request) {
        if (request == null) return null;
        return TrainingType.builder()
                .trainingTypeName(request.getTrainingTypeName())
                .build();
    }

    // Used for Controller responses
    public TrainingTypeResponse toResponse(TrainingType entity) {
        if (entity == null) return null;
        return new TrainingTypeResponse(entity.getId(), entity.getTrainingTypeName());
    }
}