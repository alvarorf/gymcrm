package com.gymcrm.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.PropertySource;

@ComponentScan(basePackages = "com.gymcrm") // To scan for @Component, @Service, @Repository
@PropertySource("classpath:application.properties") // To load the applciation.properties file and thus the initial data
@Configuration
public class AppConfig {
}
