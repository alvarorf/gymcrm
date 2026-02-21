package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.core.config.SecurityConfig;
import com.gymcrm.core.logging.RestLoggingFilter;
import com.gymcrm.core.logging.TransactionFilter;
import com.gymcrm.core.security.CustomUserDetailsService;
import com.gymcrm.core.security.JwtAuthenticationFilter;
import com.gymcrm.core.util.JwtUtils;
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

    // Mocks to satisfy SecurityConfig and the Filter Chain
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;
    @MockitoBean private CustomUserDetailsService customUserDetailsService;
    @MockitoBean private JwtUtils jwtUtils;

    public TraineeControllerTest(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @BeforeEach
    void setUp() throws Exception {
        // Prevent the mocked security filter from blocking the request.
        // Without this, we will get 200 OK with an empty body ("Shadow 200" bug).
        doAnswer(invocation -> {
            jakarta.servlet.http.HttpServletRequest request = invocation.getArgument(0);
            jakarta.servlet.http.HttpServletResponse response = invocation.getArgument(1);
            jakarta.servlet.FilterChain chain = invocation.getArgument(2);
            chain.doFilter(request, response);
            return null;
        }).when(jwtAuthenticationFilter).doFilter(any(), any(), any());
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

    // --- EXCEPTION HANDLER TESTS ---

    @Test
    @DisplayName("VALIDATION FAIL: Should return 400 when registration fields are missing")
    void register_ValidationFailure() throws Exception {
        // ARRANGE
        // Missing firstName and lastName to trigger @NotBlank validation
        TraineeRegistrationRequest invalidRequest = TraineeRegistrationRequest.builder()
                .firstName("")
                .lastName(null)
                .build();

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainees/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_REG_FAILED))
                .andExpect(jsonPath("$.module").value(Nomenclature.ERR.MODULE_TRAINEE))
                .andExpect(jsonPath("$.validation_errors.firstName").value(Nomenclature.REQD.FIRST_NAME))
                .andExpect(jsonPath("$.validation_errors.lastName").value(Nomenclature.REQD.LAST_NAME));
    }

    @Test
    @DisplayName("MALFORMED JSON: Should return 400 when JSON structure is invalid")
    void anyEndpoint_MalformedJson() throws Exception {
        // ARRANGE
        String malformedJson = "{ \"firstName\": \"John\", \"lastName\": }"; // Missing value

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainees/register")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_MALFORMED_JSON))
                .andExpect(jsonPath("$.suggestion").value(Nomenclature.MSG.SUGGESTION_MALFORMED))
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    @WithMockUser
    @DisplayName("RUNTIME EXCEPTION (DELETE): Should return 404 with Deletion Impossible error type")
    void delete_RuntimeException_Mapping() throws Exception {
        // ARRANGE
        String user = "alice.smith";
        String errorMessage = "Database constraint violation";
        doThrow(new RuntimeException(errorMessage)).when(traineeService).deleteProfile(user);

        // ACT & ASSERT
        mockMvc.perform(delete("/api/trainees/{username}", user).with(csrf()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_DEL_FAILED))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @WithMockUser
    @DisplayName("RUNTIME EXCEPTION (PATCH): Should return 400 with Toggle Failed error type")
    void toggle_RuntimeException_Mapping() throws Exception {
        // ARRANGE
        ActivationRequest request = new ActivationRequest("alice.smith", true);
        String errorMessage = "Profile is already active";
        doThrow(new RuntimeException(errorMessage)).when(traineeService).toggleActivation(anyString());

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainees/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_TOGGLE_FAILED))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @WithMockUser
    @DisplayName("GENERAL EXCEPTION: Should return 500 for unhandled checked exceptions")
    void generalException_Fallback() throws Exception {
        // ARRANGE
        // Force a non-RuntimeException to trigger handleGeneralException
        when(trainerService.getUnassignedActiveTrainersByTraineeUsername(anyString()))
                .thenAnswer(invocation -> { throw new Exception("Unexpected System Error"); });

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/alice.smith/unassigned-trainers"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_INTERNAL))
                .andExpect(jsonPath("$.message").value(Nomenclature.MSG.INTERNAL_ERROR));
    }

    @Test
    @WithMockUser
    @DisplayName("UPDATE VALIDATION FAIL: Should return 400 when username or status is missing")
    void update_ValidationFailure() throws Exception {
        // ARRANGE
        // Missing username and isActive (both required for updates per Nomenclature)
        TraineeUpdateRequest invalidRequest = TraineeUpdateRequest.builder()
                .firstName("Alice")
                .lastName("Smith")
                .build();

        // ACT & ASSERT
        mockMvc.perform(put("/api/trainees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_UPDATE_REFUSED)) // Default validation error type
                .andExpect(jsonPath("$.validation_errors.username").value(Nomenclature.REQD.USERNAME))
                .andExpect(jsonPath("$.validation_errors.isActive").value(Nomenclature.REQD.IS_ACTIVE));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH VALIDATION FAIL: Should return 400 when activation status is null")
    void toggleActivation_ValidationFailure() throws Exception {
        // ARRANGE
        // Sending a request with a null isActive field
        ActivationRequest invalidRequest = new ActivationRequest("alice.smith", null);

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainees/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validation_errors.isActive").value(Nomenclature.REQD.IS_ACTIVE));
    }

    @Test
    @WithMockUser
    @DisplayName("RUNTIME EXCEPTION (GET): Should return 404 with Profile Not Found error type")
    void getProfile_RuntimeException_Mapping() throws Exception {
        // ARRANGE
        String user = "alice.smith";
        String errorMessage = Nomenclature.getNotFoundMsg(user);
        when(traineeService.selectTraineeProfile(user)).thenThrow(new RuntimeException(errorMessage));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/{username}", user))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_NOT_FOUND))
                .andExpect(jsonPath("$.message").value(errorMessage));
    }

    @Test
    @WithMockUser
    @DisplayName("RUNTIME EXCEPTION (UNKNOWN PATH): Should return 404 with Internal Error fallback")
    void unmappedMethod_RuntimeException_Fallback() throws Exception {
        // ARRANGE
        // Simulating a RuntimeException on a method/path combo not explicitly handled in handleRuntime if/else
        when(trainerService.getUnassignedActiveTrainersByTraineeUsername(anyString()))
                .thenThrow(new RuntimeException("Unexpected Database Error"));

        // ACT & ASSERT
        mockMvc.perform(get("/api/trainees/alice.smith/unassigned-trainers"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_NOT_FOUND))
                .andExpect(jsonPath("$.message").value("Unexpected Database Error"));
    }

    @Test
    @WithMockUser
    @DisplayName("PATCH VALIDATION: Should use specialized handlePatchActivationValidation response")
    void toggle_SpecializedValidation_ReturnsToggleFailed() throws Exception {
        // ARRANGE
        ActivationRequest invalidRequest = new ActivationRequest("alice.smith", null);

        // ACT & ASSERT
        mockMvc.perform(patch("/api/trainees/activation")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                // Verify the specialized handler was used
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_TOGGLE_FAILED))
                .andExpect(jsonPath("$.details").value(Nomenclature.REQD.IS_ACTIVE))
                .andExpect(jsonPath("$.validation_errors.isActive").value(Nomenclature.REQD.IS_ACTIVE));
    }
}