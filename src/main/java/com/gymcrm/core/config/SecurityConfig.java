package com.gymcrm.core.config;

import com.gymcrm.core.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

// Note: BCrypt is a salt-based hashing algorithm by default
// It: 1. Generates a random salt and combines it with the password. 3. Hashes it multiple times 4. Stores the salt in the resulting string

import java.util.List;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
// The engine that enables AOP security, so that we can use annotations like @PreAuthorize (before method returns), @PostAuthorize
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    // Define constant arrays for better readability and maintenance
    private static final String[] PUBLIC_ASSETS = {
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/error"
    };

    private static final String[] PUBLIC_API_ENDPOINTS = {
            "/api/auth/login",
            "/api/trainees/register",
            "/api/trainers/register"
    };

    private static final String[] MONITORING_ENDPOINTS = {
            "/actuator/health",
            "/actuator/prometheus"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ASSETS).permitAll()
                        .requestMatchers(PUBLIC_API_ENDPOINTS).permitAll()
                        // Allow access to health and metrics
                        .requestMatchers(MONITORING_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("http://localhost:3000")); // Frontend URL
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}