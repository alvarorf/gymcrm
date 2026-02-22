package com.gymcrm.mapper;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserDetails toUserDetails(Trainee trainee) {
        return User.builder()
                .username(trainee.getUsername())
                .password(trainee.getPassword())
                .roles("TRAINEE")
                .build();
    }

    public UserDetails toUserDetails(Trainer trainer) {
        return User.builder()
                .username(trainer.getUsername())
                .password(trainer.getPassword())
                .roles("TRAINER")
                .build();
    }
}
