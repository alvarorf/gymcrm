package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.config.SecurityConfig;
import com.gymcrm.config.logging.*;
import com.gymcrm.dto.*;
import com.gymcrm.exception.GlobalExceptionHandler;
import com.gymcrm.service.interfaces.*;
import com.gymcrm.util.Nomenclature;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@Import({SecurityConfig.class, TransactionFilter.class, RestLoggingFilter.class, GlobalExceptionHandler.class})
@DisplayName("Trainer controller unit tests")
class TrainerControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private TrainerService trainerService;
    @MockitoBean private TrainingService trainingService;

    // --- 1. REGISTRATION TESTS ---

    @Test
    @DisplayName("REGISTER SUCCESS: Should return 201 Created and credentials")
    void register_Success() throws Exception {
        // ARRANGE
        TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .specialization(new TrainingTypeRequest("Yoga"))
                .build();
        RegistrationResponse response = new RegistrationResponse("john.doe", "rawPass");

        when(trainerService.createProfile(any(TrainerRegistrationRequest.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainers/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"))
                .andExpect(jsonPath("$.password").value("rawPass"));
    }

    @Test
    @DisplayName("REGISTER VALIDATION: Missing firstName should return 400 Bad Request")
    void register_ValidationError() throws Exception {
        // ARRANGE
        TrainerRegistrationRequest invalidRequest = new TrainerRegistrationRequest(); // All null

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainers/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors.firstName").value(Nomenclature.REQD.FIRST_NAME));
    }

    // --- 2. PROFILE TESTS ---

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE SUCCESS: Should return 200 OK")
    void getProfile_Success() throws Exception {
        // ARRANGE
        String username = "John.Doe";
        TrainerProfileResponse profile = TrainerProfileResponse.builder()
                .firstName("John").lastName("Doe").isActive(true)
                .build();

        when(trainerService.selectTrainerProfile(username)).thenReturn(Optional.of(profile));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}", username))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE FAILURE: Username not found should return 404")
    void getProfile_NotFound() throws Exception {
        // ARRANGE
        when(trainerService.selectTrainerProfile("unknown")).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(Nomenclature.MSG_NOT_FOUND)));
    }

    // --- 3. ACTIVATION & UPDATE ---

    @Test
    @WithMockUser
    @DisplayName("TOGGLE ACTIVATION: Should return 200 OK")
    void toggleActivation_Success() throws Exception {
        // ARRANGE
        ActivationRequest request = new ActivationRequest();
        request.setUsername("trainer.user");
        request.setIsActive(false);

        doNothing().when(trainerService).toggleActivation("trainer.user");

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainers/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    @DisplayName("UPDATE PROFILE: Should return 200 OK and updated data")
    void updateProfile_Success() throws Exception {
        // ARRANGE - We MUST provide valid data to pass @Valid checks
        TrainerUpdateRequest request = new TrainerUpdateRequest();
        request.setUsername("trainer.doe");
        request.setFirstName("John");
        request.setLastName("Doe");
        request.setIsActive(true);

        TrainerProfileResponse response = TrainerProfileResponse.builder()
                .firstName("John")
                .lastName("Doe")
                .isActive(true)
                .build();

        when(trainerService.updateProfile(any(TrainerUpdateRequest.class))).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(put("/api/trainers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    // --- 4. TRAININGS LIST ---

    @Test
    @WithMockUser
    @DisplayName("GET TRAININGS: Should return list of trainings with filters")
    void getTrainerTrainings_Success() throws Exception {
        // ARRANGE
        String username = "trainer.user";
        TrainerTrainingResponse training = new TrainerTrainingResponse(); // Mock response DTO
        when(trainingService.getTrainerTrainings(eq(username), any(), any(), any()))
                .thenReturn(Collections.singletonList(training));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}/trainings", username)
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31")
                        .param("traineeName", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}