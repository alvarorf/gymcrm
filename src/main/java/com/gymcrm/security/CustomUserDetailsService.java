package com.gymcrm.security;
/*


import com.gymcrm.model.Trainer;
import com.gymcrm.service.interfaces.AuthService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

// TODO: Fix warning: Class 'CustomUserDetailsService' is never used, perhaps we can use it in AuthServiceImpl, to simplify it?
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AuthService authService;

    public CustomUserDetailsService(AuthService authService) {
        this.authService = authService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {

        User user = authService.loadUserByUsername(username); // TODO: Fix error: Incompatible types. Found: 'org.springframework.security.core.userdetails.UserDetails', required: 'org.springframework.security.core.userdetails.User'

        // TODO: Fix warning: Condition 'user instanceof Trainer' is always 'false'
        String role = (user instanceof Trainer) // TODO: Fix error: Inconvertible types; cannot cast 'org.springframework.security.core.userdetails.User' to 'com.gymcrm.model.Trainer'
                ? "TRAINER"
                : "TRAINEE";

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(role)
                .build();
    }



}
 */

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.repositories.TrainerRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;

    public CustomUserDetailsService(TraineeRepository traineeRepository, TrainerRepository trainerRepository) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look in Trainees first, then Trainers
        return traineeRepository.findByUsername(username)
                .map(u -> new User(u.getUsername(), u.getPassword(), new ArrayList<>()))
                .orElseGet(() -> trainerRepository.findByUsername(username)
                        .map(u -> new User(u.getUsername(), u.getPassword(), new ArrayList<>()))
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username)));
    }
}

