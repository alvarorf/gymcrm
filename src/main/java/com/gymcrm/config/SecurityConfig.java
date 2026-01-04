package com.gymcrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
// The engine that enables AOP security, so that we can use annotations like @PreAuthorize (before method returns), @PostAuthorize
@EnableMethodSecurity
public class SecurityConfig {
    // No filterChain needed for a CLI app.
}
