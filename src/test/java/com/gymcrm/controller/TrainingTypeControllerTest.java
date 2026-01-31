package com.gymcrm.controller;

import com.gymcrm.dto.TrainingTypeResponse;
import com.gymcrm.mapper.TrainingTypeMapper;
import com.gymcrm.model.TrainingType;
import com.gymcrm.service.interfaces.TrainingTypeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingTypeController.class)
@DisplayName("Training Type Controller Unit Tests")
class TrainingTypeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TrainingTypeService trainingTypeService;
    @MockitoBean private TrainingTypeMapper trainingTypeMapper;

    @Test
    @WithMockUser
    @DisplayName("GET ALL: Should return list of training types with 200 OK")
    void getTrainingTypes_Success() throws Exception {
        // ARRANGE
        TrainingType yoga = new TrainingType();
        TrainingTypeResponse yogaResponse = new TrainingTypeResponse(1L, "Yoga");

        when(trainingTypeService.getAllTrainingTypes()).thenReturn(List.of(yoga));
        when(trainingTypeMapper.toResponse(yoga)).thenReturn(yogaResponse);

        // ACT & ASSERT
        mockMvc.perform(get("/api/training-types"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].trainingTypeName").value("Yoga"));
    }
}
