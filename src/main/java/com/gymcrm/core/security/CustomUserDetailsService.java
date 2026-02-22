package com.gymcrm.core.security;

import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.mapper.UserMapper;
import com.gymcrm.repositories.TraineeRepository;
import com.gymcrm.repositories.TrainerRepository;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TraineeRepository traineeRepository;
    private final TrainerRepository trainerRepository;
    private final UserMapper userMapper;

    public CustomUserDetailsService(TraineeRepository traineeRepository, TrainerRepository trainerRepository, UserMapper userMapper) {
        this.traineeRepository = traineeRepository;
        this.trainerRepository = trainerRepository;
        this.userMapper = userMapper;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Look in Trainees first, then Trainers
        return traineeRepository.findByUsername(username)
                .map(userMapper::toUserDetails)
                .orElseGet(() -> trainerRepository.findByUsername(username)
                        .map(userMapper::toUserDetails)
                        .orElseThrow(() -> new UsernameNotFoundException(Nomenclature.getNotFoundMsg(username))));
    }
}

