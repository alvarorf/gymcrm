package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.core.config.SecurityConfig;
import com.gymcrm.core.logging.RestLoggingFilter;
import com.gymcrm.core.logging.TransactionFilter;
import com.gymcrm.core.exception.TrainerControllerExceptionHandler;
import com.gymcrm.core.security.CustomUserDetailsService;
import com.gymcrm.core.security.JwtAuthenticationFilter;
import com.gymcrm.core.util.JwtUtils;
import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.dto.*;
import com.gymcrm.service.interfaces.TrainerService;
import com.gymcrm.service.interfaces.TrainingService;
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

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;

@WebMvcTest(TrainerController.class)
@Import({SecurityConfig.class, TransactionFilter.class, RestLoggingFilter.class, TrainerControllerExceptionHandler.class})
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL) // Replaces @Autowired for constructor injection in tests
@DisplayName("Trainer Controller Unit Tests")
class TrainerControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final TrainerController trainerController;

    @MockitoBean private TrainerService trainerService;
    @MockitoBean private TrainingService trainingService;

    // Mocks to satisfy SecurityConfig and the Filter Chain
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private JwtUtils jwtUtils;

    // Constructor injection for the test class itself
    public TrainerControllerTest(MockMvc mockMvc, ObjectMapper objectMapper, TrainerController trainerController) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.trainerController = trainerController;
    }

    @BeforeEach
    void setUp() throws Exception {
        // 1. Manually satisfy the setter injection if not handled by constructor
        trainerController.setTrainingService(trainingService);

        // 2. Prevent the mocked security filter from blocking the request.
        // Without this, we will get 200 OK with an empty body ("Shadow 200" bug).
        doAnswer(invocation -> {
            jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
            jakarta.servlet.http.HttpServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
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

    // --- 3. TRAININGS ---

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

    @Test
    @WithMockUser
    @DisplayName("GET PROFILE FAIL: Should return 404 when trainer not found")
    void getProfile_NotFound() throws Exception {
        // ARRANGE
        String username = "nonexistent.trainer";
        String errorMsg = Nomenclature.getNotFoundMsg(username);
        when(trainerService.selectTrainerProfile(username)).thenReturn(Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(errorMsg));
    }

    @Test
    @DisplayName("REGISTER VALIDATION FAIL: Should return 400 when specialization is missing")
    void register_ValidationFail() throws Exception {
        // ARRANGE
        // Missing specialization which is mandatory for Trainers
        TrainerRegistrationRequest request = TrainerRegistrationRequest.builder()
                .firstName("John").lastName("Doe")
                .specialization(null)
                .build();

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainers/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_REG_FAILED));
    }

    @Test
    @WithMockUser
    @DisplayName("UPDATE VALIDATION FAIL: Should return 400 when username is blank")
    void update_ValidationFail() throws Exception {
        // ARRANGE
        TrainerUpdateRequest request = TrainerUpdateRequest.builder()
                .username("") // Blank username
                .firstName("John").lastName("Doe")
                .isActive(true).build();

        // ACT & ASSERT
        mockMvc.perform(put("/api/trainers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_UPDATE_REFUSED))
                .andExpect(jsonPath("$.validation_errors.username").exists());;
    }

    @Test
    @WithMockUser
    @DisplayName("ACTIVATION VALIDATION FAIL: Should return 400 when isActive is null")
    void toggleActivation_ValidationFail() throws Exception {
        // ARRANGE
        ActivationRequest request = new ActivationRequest();
        request.setUsername("trainer.user");
        request.setIsActive(null); // Triggers @NotNull

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainers/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_TOGGLE_FAILED));
    }

    @Test
    @WithMockUser
    @DisplayName("TRAINING SEARCH FAIL: Should return 400 when service throws RuntimeException")
    void getTrainerTrainings_SearchError() throws Exception {
        // ARRANGE
        String username = "trainer.user";
        String exceptionMsg = "Invalid date range";
        when(trainingService.getTrainerTrainings(eq(username), any(), any(), any()))
                .thenThrow(new RuntimeException(exceptionMsg));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}/trainings", username))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_PERSISTENCE))
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString(exceptionMsg)));
    }

    @Test
    @WithMockUser
    @DisplayName("FAILURE: GET profile throws RuntimeException when user is not found")
    void getProfile_NotFound_Exception() throws Exception {
        // ARRANGE
        String username = "unknown.trainer";
        String expectedMessage = Nomenclature.getNotFoundMsg(username);
        when(trainerService.selectTrainerProfile(username)).thenReturn(java.util.Optional.empty());

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}", username))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(expectedMessage));
    }

    @Test
    @WithMockUser
    @DisplayName("FAILURE: Training list retrieval fails due to service RuntimeException")
    void getTrainerTrainings_InternalError() throws Exception {
        // ARRANGE
        String username = "trainer.user";
        String internalMsg = "Database connection timed out";
        when(trainingService.getTrainerTrainings(eq(username), any(), any(), any()))
                .thenThrow(new RuntimeException(internalMsg));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainers/{username}/trainings", username))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_PERSISTENCE))
                .andExpect(jsonPath("$.message", containsString(internalMsg)))
                .andExpect(jsonPath("$.suggestion").value(Nomenclature.ERR.SUGGESTION_TRAINER_SEARCH));
    }

    @Test
    @WithMockUser
    @DisplayName("VALIDATION: Registration specialized response check")
    void register_SpecializedValidation() throws Exception {
        // ARRANGE
        TrainerRegistrationRequest invalidRequest = TrainerRegistrationRequest.builder()
                .firstName("") // Validation error
                .build();

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainers/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_REG_FAILED))
                .andExpect(jsonPath("$.details").value(Nomenclature.ERR.DETAIL_TRAINER_SPEC));
    }
}