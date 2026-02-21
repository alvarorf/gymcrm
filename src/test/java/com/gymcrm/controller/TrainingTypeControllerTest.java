package com.gymcrm.controller;

import com.gymcrm.core.exception.TrainingTypeControllerExceptionHandler;
import com.gymcrm.core.security.JwtAuthenticationFilter;
import com.gymcrm.mapper.TrainingTypeMapper;
import com.gymcrm.service.interfaces.TrainingTypeService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TrainingTypeController.class)
@Import(TrainingTypeControllerExceptionHandler.class) // Ensure custom error structure is tested
@DisplayName("Training Type Controller Expanded Tests")
class TrainingTypeControllerTest {

    @Autowired private MockMvc mockMvc;
    @MockitoBean private TrainingTypeService trainingTypeService;
    @MockitoBean private TrainingTypeMapper trainingTypeMapper;
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
    @DisplayName("GET ALL: Should return 200 OK and an empty list when no types exist")
    void getTrainingTypes_EmptyList() throws Exception {
        // ARRANGE
        when(trainingTypeService.getAllTrainingTypes()).thenReturn(Collections.emptyList());

        // ACT
        ResultActions result = mockMvc.perform(get("/api/training-types"));

        // ASSERT
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser
    @DisplayName("GET ALL FAIL: Should return 404 when service indicates 'not found' in message")
    void getTrainingTypes_NotFound() throws Exception {
        // ARRANGE
        // The ExceptionHandler specifically checks for "not found" to return 404
        when(trainingTypeService.getAllTrainingTypes())
                .thenThrow(new RuntimeException("Training catalog not found"));

        // ACT
        ResultActions result = mockMvc.perform(get("/api/training-types"));

        // ASSERT
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error_type").value("Dictionary Retrieval Failed"))
                .andExpect(jsonPath("$.suggestion").value("Verify database seed data for 'TrainingType' table."));
    }

    @Test
    @WithMockUser
    @DisplayName("GET ALL FAIL: Should return 500 when service throws a generic runtime error")
    void getTrainingTypes_InternalError() throws Exception {
        // ARRANGE
        when(trainingTypeService.getAllTrainingTypes())
                .thenThrow(new RuntimeException("Database connection timed out"));

        // ACT
        ResultActions result = mockMvc.perform(get("/api/training-types"));

        // ASSERT
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error_type").value("Dictionary Retrieval Failed"))
                .andExpect(jsonPath("$.message").value("Database connection timed out"));
    }

    @Test
    @WithMockUser
    @DisplayName("GET ALL FAIL: Should return 500 with custom message for checked exceptions")
    void getTrainingTypes_GeneralFailure() throws Exception {
        // ARRANGE
        // Simulating a failure handled by the handleGeneralFailure method
        when(trainingTypeService.getAllTrainingTypes())
                .thenAnswer(invocation -> { throw new Exception("Persistence provider down"); });

        // ACT
        ResultActions result = mockMvc.perform(get("/api/training-types"));

        // ASSERT
        result.andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error_type").value("System Metadata Error"))
                .andExpect(jsonPath("$.message").value("The training types catalog is currently unavailable."));
    }
}