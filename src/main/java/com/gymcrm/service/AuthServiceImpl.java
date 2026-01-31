package com.gymcrm.service;

import com.gymcrm.dao.interfaces.*;
import com.gymcrm.security.CustomUserDetailsService;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.util.Nomenclature;
import org.slf4j.*;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.authentication.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;

@Service
public class AuthServiceImpl implements AuthService {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(TraineeDao traineeDao,
                           TrainerDao trainerDao,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
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
        return customUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Nomenclature.info(logger, Nomenclature.Action.UPDATE_SENSITIVE, username);

        // Authenticate the user with the old password
        // This will throw an AuthenticationException if the old password doesn't match
        authenticate(username, oldPassword);

        // Encode the password before saving!
        String encodedPassword = passwordEncoder.encode(newPassword);

        // If authentication passed, update the user in the database
        // The newPassword should be encoded here via PasswordEncoder
        boolean updated = updatePasswordInStorage(username, encodedPassword);

        if (!updated) {  throw new UsernameNotFoundException(Nomenclature.getNotFoundMsg(username));  }

        Nomenclature.success(logger, Nomenclature.Action.UPDATE_SENSITIVE, username);
    }

    private boolean updatePasswordInStorage(String username, String newPassword) {
        return traineeDao.findByUsername(username)
                .map(t -> { t.setPassword(newPassword); traineeDao.save(t); return true; })
                .orElseGet(() -> trainerDao.findByUsername(username)
                        .map(t -> { t.setPassword(newPassword); trainerDao.save(t); return true; })
                        .orElse(false));
    }


}
