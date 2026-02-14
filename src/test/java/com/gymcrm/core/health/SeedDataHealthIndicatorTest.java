package com.gymcrm.core.health;

import com.gymcrm.core.util.Nomenclature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.Status;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SeedDataHealthIndicatorTest {

    private SeedDataHealthIndicator healthIndicator;

    @BeforeEach
    void setUp() {
        healthIndicator = new SeedDataHealthIndicator();
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "initial-data/initial-data.json",
            "initial-data/initial-dev-data.json",
            "initial-data/initial-stg-data.json",
            "initial-data/initial-prod-data.json"
    })
    @DisplayName("UP Status: Should work for any environment-specific file that exists")
    void health_ExistingEnvironmentFiles_ReturnsUp(String envPath) {
        // ARRANGE
        // In a unit test, we ensure the logic handles the path provided by the profile
        ReflectionTestUtils.setField(healthIndicator, "dataPath", envPath);

        // ACT
        Health result = healthIndicator.health();

        // ASSERT
        // Note: This test assumes the files actually exist in src/main/resources
        assertEquals(Status.UP, result.getStatus());
        assertEquals(envPath, result.getDetails().get("file"));
        assertEquals(Nomenclature.MSG.HEALTH_UP, result.getDetails().get(Nomenclature.ERR.KEY_MESSAGE));
    }

    @Test
    @DisplayName("DOWN Status: Should return DOWN if the specific environment file is missing")
    void health_WrongPath_ReturnsDown() {
        // ARRANGE
        String wrongPath = "initial-data/broken-link.json";
        ReflectionTestUtils.setField(healthIndicator, "dataPath", wrongPath);

        // ACT
        Health result = healthIndicator.health();

        // ASSERT
        assertEquals(Status.DOWN, result.getStatus());
        assertEquals(Nomenclature.MSG.HEALTH_DOWN, result.getDetails().get(Nomenclature.ERR.KEY_ERR_TYPE));
    }
}