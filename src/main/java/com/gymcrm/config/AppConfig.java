package com.gymcrm.config;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.model.Training;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

import java.util.HashMap;
import java.util.Map;

@ComponentScan(basePackages = "com.gymcrm") // To scan for @Component, @Service, @Repository
@PropertySource("classpath:application.properties") // To load the application.properties file and thus the initial data
@Configuration
public class AppConfig {
    // Requirement: Every storage (java.util.Map) implemented as a separate bean.

    @Bean("traineeMap")
    public Map<Long, Trainee> traineeMap() {
        return new HashMap<>();
    }

    @Bean("trainerMap")
    public Map<Long, Trainer> trainerMap() {
        return new HashMap<>();
    }

    @Bean("trainingMap")
    public Map<Long, Training> trainingMap() {
        return new HashMap<>();
    }
}
