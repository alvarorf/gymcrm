package com.gymcrm.core.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("OpenAPI Configuration Unit Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    @Test
    @DisplayName("OPENAPI BEAN: Should initialize with correct metadata and title")
    void customOpenAPI_ShouldHaveCorrectMetadata() {
        // ARRANGE
        String expectedTitle = "Gym CRM API";
        String expectedVersion = "1.0";

        // ACT
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // ASSERT
        assertNotNull(openAPI, "OpenAPI bean should not be null");
        assertEquals(expectedTitle, openAPI.getInfo().getTitle());
        assertEquals(expectedVersion, openAPI.getInfo().getVersion());
        assertTrue(openAPI.getInfo().getDescription().contains("Spring 6"), "Description should mention environment");
    }

    @Test
    @DisplayName("SECURITY SCHEME: Should configure JWT Bearer Auth correctly")
    void customOpenAPI_ShouldContainBearerAuthScheme() {
        // ARRANGE
        String schemeName = "bearerAuth";

        // ACT
        OpenAPI openAPI = openApiConfig.customOpenAPI();
        SecurityScheme scheme = openAPI.getComponents().getSecuritySchemes().get(schemeName);

        // ASSERT
        assertNotNull(scheme, "Security scheme 'bearerAuth' should be defined");
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
    }

    @Test
    @DisplayName("SECURITY REQUIREMENT: Should apply the security item globally")
    void customOpenAPI_ShouldHaveGlobalSecurityRequirement() {
        // ARRANGE
        String expectedScheme = "bearerAuth";

        // ACT
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // ASSERT
        assertFalse(openAPI.getSecurity().isEmpty(), "Security requirements list should not be empty");
        assertTrue(openAPI.getSecurity().get(0).containsKey(expectedScheme),
                "Global security should require 'bearerAuth'");
    }
}