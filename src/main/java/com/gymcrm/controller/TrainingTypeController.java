package com.gymcrm.controller;

import com.gymcrm.dto.TrainingTypeResponse;
import com.gymcrm.mapper.TrainingTypeMapper;
import com.gymcrm.service.interfaces.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Dictionary", description = "Training Type metadata")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;
    private final TrainingTypeMapper trainingTypeMapper;

    public TrainingTypeController(TrainingTypeService trainingTypeService, TrainingTypeMapper trainingTypeMapper) {
        this.trainingTypeService = trainingTypeService;
        this.trainingTypeMapper = trainingTypeMapper;
    }

    @Operation(summary = "Get all available training types")
    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingTypeResponse> types = trainingTypeService.getAllTrainingTypes().stream()
                .map(trainingTypeMapper::toResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(types);
    }
}