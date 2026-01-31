package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.dto.TrainingCreateRequest;
import com.gymcrm.service.interfaces.TrainingService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@DisplayName("Training Controller Validation Tests")
class TrainingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private TrainingService trainingService;

    @Test
    @WithMockUser
    @DisplayName("SUCCESS: Should return 200 when all mandatory fields are present")
    void addTraining_Ok() throws Exception {
        // ARRANGE
        TrainingCreateRequest request = createValidRequest();

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(trainingService).createProfile(any(TrainingCreateRequest.class));
    }

    @Test
    @WithMockUser
    @DisplayName("FAILURE: Should return 400 when trainingName is blank")
    void addTraining_MissingName() throws Exception {
        // ARRANGE
        TrainingCreateRequest request = createValidRequest();
        request.setTrainingName(""); // Violation of @NotBlank

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        // Verify the service was NEVER called due to validation failure
        verify(trainingService, never()).createProfile(any());
    }

    @Test
    @WithMockUser
    @DisplayName("FAILURE: Should return 400 when trainingDate is null")
    void addTraining_NullDate() throws Exception {
        // ARRANGE
        TrainingCreateRequest request = createValidRequest();
        request.setTrainingDate(null); // Violation of @NotNull

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(trainingService, never()).createProfile(any());
    }

    // Helper method to keep tests DRY
    private TrainingCreateRequest createValidRequest() {
        TrainingCreateRequest request = new TrainingCreateRequest();
        request.setTraineeUsername("john.doe");
        request.setTrainerUsername("coach.smith");
        request.setTrainingName("Power Lifting");
        request.setTrainingDate(LocalDate.now().plusDays(1));
        request.setTrainingDuration(60);
        return request;
    }
}