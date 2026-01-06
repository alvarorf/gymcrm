package com.gymcrm.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;


@ComponentScan(basePackages = "com.gymcrm") // To scan for @Component, @Service, @Repository
@PropertySource("classpath:application.properties") // To load the application.properties file and thus the initial data
@Configuration
public class AppConfig {
    // Required to resolve ${storage.initial-data-file} in Spring
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
