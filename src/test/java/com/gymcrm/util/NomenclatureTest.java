package com.gymcrm.util;

import com.gymcrm.core.util.Nomenclature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Nomenclature Utility Unit Tests")
class NomenclatureTest {

    @Mock
    private Logger logger;

    @BeforeEach
    void setUp() {
        // Ensure logger is enabled so the methods don't skip execution
        lenient().when(logger.isInfoEnabled()).thenReturn(true);
        lenient().when(logger.isWarnEnabled()).thenReturn(true);
    }

    // --- 1. STRING UTILITY METHODS ---

    @Test
    @DisplayName("getNotFoundMsg(String): Should concatenate identifier with standard message")
    void getNotFoundMsg_String_Success() {
        // ACT
        String result = Nomenclature.getNotFoundMsg("user123");

        // ASSERT
        assertEquals(Nomenclature.MSG_NOT_FOUND + "user123", result);
    }

    @Test
    @DisplayName("getNotFoundMsg(Class): Should return simple class name with 'not found'")
    void getNotFoundMsg_Class_Success() {
        // ACT
        String result = Nomenclature.getNotFoundMsg(Nomenclature.class);

        // ASSERT
        assertEquals("Nomenclature not found", result);
    }

    // --- 2. LOGGING LOGIC & CLASS NAME TRIMMING ---

    @Test
    @DisplayName("info(): Should trim 'ServiceImpl' from class name and log attempt")
    void info_TrimsClassNameAndLogs() {
        // ARRANGE
        MockServiceImpl mockService = new MockServiceImpl();

        // ACT
        mockService.triggerInfo(logger);

        // ASSERT
        // Pattern: [{}] Attempting to {} {}
        verify(logger).info(eq("[{}] Attempting to {} {}"), eq("triggerInfo"), eq("create"), eq("Mock"));
    }

    @Test
    @DisplayName("success(): Should format success message with identifier and trimmed class name")
    void success_LogsFormattedMessage() {
        // ARRANGE
        MockServiceImpl mockService = new MockServiceImpl();

        // ACT
        mockService.triggerSuccess(logger, "test-id");

        // ASSERT
        // Pattern: [{}] Successfully {} {}
        verify(logger).info(eq("[{}] Successfully {} {}"), eq("triggerSuccess"), eq("update"), eq("Mock (test-id)"));
    }

    @Test
    @DisplayName("warn(): Should log warning with class name and identifier")
    void warn_LogsWarningMessage() {
        // ARRANGE
        MockServiceImpl mockService = new MockServiceImpl();

        // ACT
        mockService.triggerWarn(logger, "404-id");

        // ASSERT
        verify(logger).warn(eq("[{}] {} not found with identifier: {}"), eq("triggerWarn"), eq("Mock"), eq("404-id"));
    }

    // --- 3. DICTIONARY (getActionText) COVERAGE ---

    @Test
    @DisplayName("DICTIONARY: FETCH should yield 'retrieve all' when method starts with 'findAll'")
    void getActionText_Fetch_All() {
        // ARRANGE
        MockServiceImpl mockService = new MockServiceImpl();

        // ACT
        mockService.findAllTest(logger);

        // ASSERT
        // Position 0: The Template
        // Position 1: The Method Name
        // Position 2: The Action Text (What we actually want to test)
        // Position 3: The Subject
        verify(logger).info(
                anyString(),
                eq("findAllTest"),
                eq("retrieve all records for"),
                eq("Mock")
        );
    }

    @Test
    @DisplayName("DICTIONARY: TOGGLE should include status in message")
    void getActionText_Toggle_WithStatus() {
        // ARRANGE
        MockServiceImpl mockService = new MockServiceImpl();

        // ACT
        Nomenclature.info(logger, Nomenclature.Action.TOGGLE, "ACTIVE");

        // ASSERT
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).info(anyString(), anyString(), captor.capture(), anyString());
        assertEquals("change status to ACTIVE", captor.getValue());
    }

    @Test
    @DisplayName("DICTIONARY: UPDATE_SENSITIVE should mention password update")
    void getActionText_UpdateSensitive() {
        // ARRANGE
        MockServiceImpl mockService = new MockServiceImpl();

        // ACT
        Nomenclature.info(logger, Nomenclature.Action.UPDATE_SENSITIVE, "userX");

        // ASSERT
        ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
        verify(logger).info(anyString(), anyString(), captor.capture(), anyString());
        assertTrue(captor.getValue().contains("sensitive data (password) for userX"));
    }

    // --- 4. CONSTRUCTOR LOGGING ---

    @Test
    @DisplayName("info(): Should log special CONSTRUCTOR message when called from <init>")
    void info_Constructor_Success() {
        // ACT
        new MockServiceImpl(logger);

        // ASSERT
        verify(logger).info(eq("[{}] Context initialized for {}"), eq("CONSTRUCTOR"), eq("Mock"));
    }

    @Test
    @DisplayName("DICTIONARY: SEED should yield 'seed database...'")
    void getActionText_Seed() {
        // ACT
        Nomenclature.info(logger, Nomenclature.Action.SEED);

        // ASSERT
        verify(logger).info(anyString(), anyString(), eq("seed database with initial data"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: INITIALIZE should yield 'initialize'")
    void getActionText_Initialize() {
        // ACT
        Nomenclature.info(logger, Nomenclature.Action.INITIALIZE);

        // ASSERT
        verify(logger).info(anyString(), anyString(), eq("initialize"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: DELETE should yield 'delete'")
    void getActionText_Delete() {
        Nomenclature.info(logger, Nomenclature.Action.DELETE);
        verify(logger).info(anyString(), anyString(), eq("delete"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: FETCH with 'ALL' context should yield 'retrieve all'")
    void getActionText_Fetch_WithAllContext() {
        // Even if method doesn't start with findAll, context "ALL" should trigger it
        Nomenclature.info(logger, Nomenclature.Action.FETCH, "ALL");

        verify(logger).info(anyString(), anyString(), eq("retrieve all records for"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: FETCH with no context should yield default 'record'")
    void getActionText_Fetch_NoContext() {
        MockServiceImpl mockService = new MockServiceImpl();
        mockService.triggerFetchNoContext(logger);

        verify(logger).info(anyString(), anyString(), eq("retrieve context for record"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: TOGGLE with no context should yield 'alternate'")
    void getActionText_Toggle_NoContext() {
        Nomenclature.info(logger, Nomenclature.Action.TOGGLE);

        verify(logger).info(anyString(), anyString(), eq("change status to alternate"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: UPDATE_SENSITIVE with no context should yield default 'user'")
    void getActionText_UpdateSensitive_NoContext() {
        Nomenclature.info(logger, Nomenclature.Action.UPDATE_SENSITIVE);

        verify(logger).info(anyString(), anyString(), contains("for user"), anyString());
    }

    @Test
    @DisplayName("DICTIONARY: AUTH should yield 'authenticate'")
    void getActionText_Auth() {
        Nomenclature.info(logger, Nomenclature.Action.AUTH);
        verify(logger).info(anyString(), anyString(), eq("authenticate"), anyString());
    }

    @Test
    @DisplayName("LOGGING: info() should not interact with logger if info level is disabled")
    void info_Disabled_DoesNothing() {
        // ARRANGE
        reset(logger);
        when(logger.isInfoEnabled()).thenReturn(false);

        // ACT
        Nomenclature.info(logger, Nomenclature.Action.CREATE);

        // ASSERT
        verify(logger, never()).info(anyString(), anyString(), anyString(), anyString());
    }
    
    // --- HELPER CLASSES TO SIMULATE CALL STACK ---

    private static class MockServiceImpl {
        public MockServiceImpl() {}

        // Constructor test
        public MockServiceImpl(Logger logger) {
            Nomenclature.info(logger, Nomenclature.Action.INITIALIZE);
        }

        public void triggerInfo(Logger logger) {
            Nomenclature.info(logger, Nomenclature.Action.CREATE);
        }

        public void triggerSuccess(Logger logger, Object id) {
            Nomenclature.success(logger, Nomenclature.Action.UPDATE, id);
        }

        public void triggerWarn(Logger logger, Object id) {
            Nomenclature.warn(logger, Nomenclature.Action.DELETE, id);
        }

        public void findAllTest(Logger logger) {
            Nomenclature.info(logger, Nomenclature.Action.FETCH);
        }

        public void triggerFetchNoContext(Logger logger) {
            // Method name does NOT start with findAll
            Nomenclature.info(logger, Nomenclature.Action.FETCH);
        }
    }
}