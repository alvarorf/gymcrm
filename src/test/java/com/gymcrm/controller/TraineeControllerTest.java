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

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
@Import({SecurityConfig.class, TransactionFilter.class, RestLoggingFilter.class, GlobalExceptionHandler.class})
@DisplayName("Trainee controller unit tests")
class TraineeControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private TraineeService traineeService;
    @MockitoBean private TrainerService trainerService;

    // --- 1. REGISTRATION ---

    @Test
    @DisplayName("REGISTER SUCCESS: Valid request returns 201 and credentials")
    void register_Success() throws Exception {
        // ARRANGE
        TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                .firstName("Alice").lastName("Smith")
                .dateOfBirth(LocalDate.of(2000, 1, 1))
                .address("123 Gym St")
                .build();

        RegistrationResponse response = new RegistrationResponse("alice.smith", "pass123");
        when(traineeService.createProfile(any())).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainees/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("alice.smith"));
    }

    @Test
    @DisplayName("REGISTER FAILURE: Blank names should return 400 Bad Request")
    void register_ValidationFail() throws Exception {
        // ARRANGE: Empty names violate @NotBlank in TraineeRegistrationRequest
        TraineeRegistrationRequest invalid = TraineeRegistrationRequest.builder()
                .firstName("").lastName(" ")
                .build();

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainees/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors.firstName").value(Nomenclature.REQD.FIRST_NAME));
    }

    // --- 2. PROFILE RETRIEVAL ---

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE SUCCESS: Returns 200 and profile data")
    void getProfile_Success() throws Exception {
        // ARRANGE
        TraineeProfileResponse profile = TraineeProfileResponse.builder()
                .firstName("Alice").isActive(true).trainers(Collections.emptyList())
                .build();
        when(traineeService.selectTraineeProfile("alice.smith")).thenReturn(Optional.of(profile));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/alice.smith"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE FAILURE: Non-existent user returns 404")
    void getProfile_NotFound() throws Exception {
        // ARRANGE: Mock returning empty optional to trigger the orElseThrow in controller
        when(traineeService.selectTraineeProfile("unknown")).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/unknown"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value(containsString(Nomenclature.MSG_NOT_FOUND)));
    }

    // --- 3. UPDATE ---

    @Test
    @WithMockUser
    @DisplayName("UPDATE SUCCESS: Valid data returns 200 OK")
    void update_Success() throws Exception {
        // ARRANGE: All mandatory fields from TraineeUpdateRequest must be present
        TraineeUpdateRequest request = TraineeUpdateRequest.builder()
                .username("alice.smith").firstName("Alice").lastName("Revised")
                .isActive(true)
                .build();

        TraineeProfileResponse response = TraineeProfileResponse.builder().lastName("Revised").build();
        when(traineeService.updateProfile(any())).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(put("/api/trainees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Revised"));
    }

    // --- 4. DELETE & ACTIVATION ---

    @Test
    @WithMockUser
    @DisplayName("DELETE SUCCESS: Returns 200 OK")
    void delete_Success() throws Exception {
        // ACT & ASSERT
        mockMvc.perform(delete("/api/trainees/alice.smith").with(csrf()))
                .andExpect(status().isOk());

        verify(traineeService, times(1)).deleteProfile("alice.smith");
    }

    @Test
    @WithMockUser
    @DisplayName("ACTIVATION SUCCESS: Valid request returns 200 OK")
    void toggleActivation_Success() throws Exception {
        // ARRANGE
        ActivationRequest request = new ActivationRequest();
        request.setUsername("alice.smith");
        request.setIsActive(false);

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainees/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- 5. UNASSIGNED TRAINERS ---

    @Test
    @WithMockUser
    @DisplayName("GET UNASSIGNED TRAINERS: Returns list of trainers")
    void getUnassignedTrainers_Success() throws Exception {
        // ARRANGE
        TrainerShortResponse trainer = new TrainerShortResponse(); // Mocked short response
        when(trainerService.getUnassignedActiveTrainersByTraineeUsername("alice.smith"))
                .thenReturn(Collections.singletonList(trainer));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/alice.smith/unassigned-trainers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }
}