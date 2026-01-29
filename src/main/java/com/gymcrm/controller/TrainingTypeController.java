package com.gymcrm.controller;

import com.gymcrm.dto.TrainingTypeResponse;
import com.gymcrm.service.interfaces.TrainingTypeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/training-types")
@Tag(name = "Dictionary", description = "Training Type metadata")
public class TrainingTypeController {

    private final TrainingTypeService trainingTypeService;

    public TrainingTypeController(TrainingTypeService trainingTypeService) {
        this.trainingTypeService = trainingTypeService;
    }

    @Operation(summary = "Get all available Training Types")
    @GetMapping
    public ResponseEntity<List<TrainingTypeResponse>> getTrainingTypes() {
        List<TrainingTypeResponse> types = trainingTypeService.getAllTrainingTypes().stream()
                .map(t -> new TrainingTypeResponse(t.getId(), t.getTrainingTypeName()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(types);
    }
}