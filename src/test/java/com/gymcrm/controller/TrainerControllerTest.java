package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.config.SecurityConfig;
import com.gymcrm.config.logging.RestLoggingFilter;
import com.gymcrm.config.logging.TransactionFilter;
import com.gymcrm.dto.*;
import com.gymcrm.exception.GlobalExceptionHandler;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
import com.gymcrm.util.Nomenclature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainerController.class)
@Import({SecurityConfig.class, TransactionFilter.class, RestLoggingFilter.class, GlobalExceptionHandler.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) // Replaces @Autowired for constructor injection in tests
@DisplayName("Trainer Controller Unit Tests")
class TrainerControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TrainerController trainerController;

    @MockitoBean private TrainerService trainerService;
    @MockitoBean private TrainingService trainingService;

    // Constructor injection for the test class itself
    public TrainerControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, TrainerController trainerController) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.trainerController = trainerController;
    }

    @BeforeEach
    void setUp() {
        // Manually satisfy the setter injection because the controller field is not @Autowired
        trainerController.setTrainingService(trainingService);
    }

    // --- 1. REGISTRATION ---

    @Test
    @DisplayName("REGISTER SUCCESS: Valid request returns 201")
    void register_Success() throws Exception {
        // ARRANGE
        TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .specialization(new TrainingTypeRequest("Yoga"))
                .build();
        RegistrationResponse response = new RegistrationResponse("john.doe", "pass123");
        when(trainerService.createProfile(any())).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainers/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value("john.doe"));
    }

    // --- 2. PROFILE ---

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE SUCCESS: Returns 200 and data")
    void getProfile_Success() throws Exception {
        // ARRANGE
        TrainerProfileResponse profile = TrainerProfileResponse.builder()
                .firstName("John").isActive(true).build();
        when(trainerService.selectTrainerProfile("john.doe")).thenReturn(Optional.of(profile));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/john.doe"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"));
    }

    @Test
    @WithMockUser
    @DisplayName("UPDATE PROFILE: Valid data returns 200 OK")
    void update_Success() throws Exception {
        // ARRANGE
        TrainerUpdateRequest request = TrainerUpdateRequest.builder()
                .username("john.doe").firstName("John").lastName("Doe")
                .isActive(true).build();
        TrainerProfileResponse response = TrainerProfileResponse.builder().firstName("John").build();
        when(trainerService.updateProfile(any())).thenReturn(response);

        // ACT & ASSERT
        mockMvc.perform(put("/api/trainers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    // --- 3. TRAININGS (FIXED NPE) ---

    @Test
    @WithMockUser
    @DisplayName("GET TRAININGS: Valid criteria returns list")
    void getTrainerTrainings_Success() throws Exception {
        // ARRANGE
        String username = "trainer.user";
        TrainerTrainingResponse training = TrainerTrainingResponse.builder()
                .trainingName("Morning Yoga")
                .trainingDate(LocalDate.now())
                .trainingType("Yoga")
                .trainingDuration(60)
                .traineeName("Alice Smith")
                .build();

        when(trainingService.getTrainerTrainings(eq(username), any(), any(), any()))
                .thenReturn(Collections.singletonList(training));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}/trainings", username)
                        .param("from", "2024-01-01")
                        .param("to", "2024-12-31")
                        .param("traineeName", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].trainingName").value("Morning Yoga"));
    }

    @Test
    @WithMockUser
    @DisplayName("ACTIVATION: Patch returns 200 OK")
    void toggleActivation_Success() throws Exception {
        // ARRANGE
        ActivationRequest request = new ActivationRequest();
        request.setUsername("trainer.user");
        request.setIsActive(true);

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainers/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}