package com.gymcrm.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gymcrm.core.security.JwtAuthenticationFilter;
import com.gymcrm.core.util.Nomenclature;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TrainingController.class)
@DisplayName("Training Controller Validation Tests")
class TrainingControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @MockitoBean private TrainingService trainingService;
    @MockitoBean private JwtAuthenticationFilter jwtAuthenticationFilter;

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

    @Test
    @WithMockUser
    @DisplayName("EXCEPTION: Should return 422 when JSON contains malformed data types")
    void addTraining_MalformedJson() throws Exception {
        // ARRANGE
        // Sending a string "sixty" instead of an integer 60 for duration
        String malformedJson = """
            {
                "traineeUsername": "john.doe",
                "trainerUsername": "coach.smith",
                "trainingName": "Yoga",
                "trainingDate": "2026-02-14",
                "trainingDuration": "sixty"
            }
            """;

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(malformedJson))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_INVALID_FORMAT))
                .andExpect(jsonPath("$.details").value(Nomenclature.ERR.DETAIL_INCOMPATIBLE_TYPES));
    }

    @Test
    @WithMockUser
    @DisplayName("EXCEPTION: Should return 404 when service throws RuntimeException (User Not Found)")
    void addTraining_UserNotFound() throws Exception {
        // ARRANGE
        TrainingCreateRequest request = createValidRequest();
        String errorMessage = "Trainee not found";

        // Mock service layer failure
        doThrow(new RuntimeException(errorMessage))
                .when(trainingService).createProfile(any(TrainingCreateRequest.class));

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_PERSISTENCE))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.suggestion").value(Nomenclature.ERR.SUGGESTION_USER_VERIFY));
    }

    @Test
    @WithMockUser
    @DisplayName("VALIDATION: Should include specialized details on validation failure")
    void addTraining_ValidationDetails() throws Exception {
        // ARRANGE
        TrainingCreateRequest request = createValidRequest();
        request.setTrainingName(null);

        // ACT & ASSERT
        mockMvc.perform(post("/api/trainings")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error_type").value(Nomenclature.ERR.TYPE_TRAINING_DENIED))
                .andExpect(jsonPath("$.details").value(Nomenclature.ERR.DETAIL_TRAINING_MISSING))
                .andExpect(jsonPath("$.suggestion").value(Nomenclature.ERR.SUGGESTION_TRAINING_REQD));
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