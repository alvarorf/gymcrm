package com.gymcrm.service;

import com.gymcrm.dao.interfaces.*;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.util.Nomenclature;
import org.slf4j.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.*;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

@Service
public class AuthServiceImpl implements AuthService {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final AuthenticationManager authenticationManager;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(TraineeDao traineeDao,
                           TrainerDao trainerDao,
                           AuthenticationManager authenticationManager) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationManager = authenticationManager;
    }

    @Override
    public UserDetails authenticate(String username, String password) {
        Nomenclature.info(logger, Nomenclature.Action.AUTH);

        // This delegates password matching and user loading to Spring Security's internal providers
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password)
        );

        // If successful, the principal is our UserDetails object
        return (UserDetails) authentication.getPrincipal();
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Query MySQL via DAOs
        return traineeDao.findByUsername(username)
                .map(t -> User.builder()
                        .username(t.getUsername())
                        .password(t.getPassword()) // Should be encoded in DB
                        .roles("TRAINEE")
                        .build())
                .orElseGet(() -> trainerDao.findByUsername(username)
                        .map(t -> User.builder()
                                .username(t.getUsername())
                                .password(t.getPassword())
                                .roles("TRAINER")
                                .build())
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username))
                );
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) { // TODO: Improve this method
        Nomenclature.info(logger, Nomenclature.Action.UPDATE_SENSITIVE, username);

        // 1. First, authenticate the user with the old password
        // This will throw an AuthenticationException if the old password doesn't match
        authenticate(username, oldPassword);

        // 2. If authentication passed, update the user in the database
        // Note: In a production app, the newPassword should be encoded here via PasswordEncoder
        boolean updated = false;

        // Check Trainees
        var traineeOpt = traineeDao.findByUsername(username);
        if (traineeOpt.isPresent()) {
            var trainee = traineeOpt.get();
            trainee.setPassword(newPassword);
            traineeDao.save(trainee);
            updated = true;
        }

        // If not found in trainees, check Trainers
        if (!updated) {
            var trainerOpt = trainerDao.findByUsername(username);
            if (trainerOpt.isPresent()) {
                var trainer = trainerOpt.get();
                trainer.setPassword(newPassword);
                trainerDao.save(trainer);
                updated = true;
            }
        }

        if (!updated) {
            throw new UsernameNotFoundException(Nomenclature.getNotFoundMsg(username.getClass()));
        }

        Nomenclature.success(logger, Nomenclature.Action.UPDATE_SENSITIVE, username);
    }
}
