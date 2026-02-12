package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.core.config.SecurityConfig;
import com.gymcrm.core.config.logging.*;
import com.gymcrm.dto.*;
import com.gymcrm.core.exception.TraineeControllerExceptionHandler;
import com.gymcrm.service.interfaces.*;
import com.gymcrm.core.util.Nomenclature;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

// Missing static imports for full coverage
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TraineeController.class)
@Import({
        SecurityConfig.class,
        TransactionFilter.class,
        RestLoggingFilter.class,
        TraineeControllerExceptionHandler.class
})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
@DisplayName("Trainee Controller Tests - Comprehensive Coverage")
class TraineeControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    @MockitoBean private TraineeService traineeService;
    @MockitoBean private TrainerService trainerService;

    public TraineeControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    // --- REGISTRATION ---

    @Test
    @DisplayName("REGISTER SUCCESS: Should return 201 and credentials")
    void register_Success() throws Exception {
        // ARRANGE
        TraineeRegistrationRequest request = TraineeRegistrationRequest.builder()
                .firstName("John").lastName("Doe").build();
        RegistrationResponse response = new RegistrationResponse("John.Doe", "p@ssword");
        when(traineeService.createProfile(any())).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainees/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("John.Doe"));
    }

    // --- PROFILE RETRIEVAL ---

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE SUCCESS: Should return 200 and profile data")
    void getProfile_Success() throws Exception {
        // ARRANGE
        String user = "alice.smith";
        TraineeProfileResponse response = TraineeProfileResponse.builder()
                .firstName("Alice")
                .lastName("Smith")
                .isActive(true)
                .build();

        when(traineeService.selectTraineeProfile(user)).thenReturn(Optional.of(response));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/{username}", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Smith"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE FAIL: Should return 404 when user not found")
    void getProfile_NotFound() throws Exception {
        // ARRANGE
        String user = "unknown.user";
        when(traineeService.selectTraineeProfile(user)).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/{username}", user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_NOT_FOUND));
    }

    // --- UPDATE ---

    @Test
    @WithMockUser
    @DisplayName("UPDATE SUCCESS: Should update profile and return 200")
    void update_Success() throws Exception {
        // ARRANGE
        TraineeUpdateRequest request = TraineeUpdateRequest.builder()
                .username("alice.smith").firstName("Alice").lastName("Revised").isActive(true).build();
        when(traineeService.updateProfile(any())).thenReturn(TraineeProfileResponse.builder().lastName("Revised").build());

        // ACT & ASSERT
        mockMvc.perform(put("/api/trainees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("Revised"));
    }

    // --- ACTIVATION (PATCH) ---

    @Test
    @WithMockUser
    @DisplayName("TOGGLE ACTIVATION SUCCESS: Should return 200")
    void toggleActivation_Success() throws Exception {
        // ARRANGE
        ActivationRequest request = new ActivationRequest("alice.smith", true);
        doNothing().when(traineeService).toggleActivation("alice.smith");

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainees/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- DELETE ---

    @Test
    @WithMockUser
    @DisplayName("DELETE SUCCESS: Should return 200")
    void delete_Success() throws Exception {
        // ARRANGE
        String user = "alice.smith";
        doNothing().when(traineeService).deleteProfile(user);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/trainees/{username}", user).with(csrf()))
                .andExpect(status().isOk());
    }

    // --- TRAINER ASSOCIATION ---

    @Test
    @WithMockUser
    @DisplayName("GET UNASSIGNED TRAINERS: Should return list of trainers")
    void getUnassignedTrainers_Success() throws Exception {
        // ARRANGE
        String user = "alice.smith";
        // TrainerShortResponse has @Builder, let's use it to avoid TrainingType object issues
        TrainerShortResponse trainer = TrainerShortResponse.builder()
                .username("john.pro")
                .firstName("John")
                .lastName("Pro")
                .build();

        when(trainerService.getUnassignedActiveTrainersByTraineeUsername(user))
                .thenReturn(List.of(trainer));
        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/{username}/unassigned-trainers", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username").value("john.pro"));
    }
}