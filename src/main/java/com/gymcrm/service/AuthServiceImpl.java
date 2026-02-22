package com.gymcrm.service;

import com.gymcrm.core.security.CustomUserDetailsService;
import com.gymcrm.core.util.JwtUtils;
import com.gymcrm.core.util.Nomenclature;
import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.service.interfaces.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.*;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final CustomUserDetailsService customUserDetailsService;
    private final LoginAttemptServiceImpl loginAttemptServiceImpl;
    private final JwtUtils jwtUtils;

    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(TraineeDao traineeDao,
                           TrainerDao trainerDao,
                           AuthenticationManager authenticationManager,
                           PasswordEncoder passwordEncoder, CustomUserDetailsService customUserDetailsService, LoginAttemptServiceImpl loginAttemptServiceImpl, JwtUtils jwtUtils) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
        this.authenticationManager = authenticationManager;
        this.passwordEncoder = passwordEncoder;
        this.customUserDetailsService = customUserDetailsService;
        this.loginAttemptServiceImpl = loginAttemptServiceImpl;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public String authenticate(String username, String password) {
        Nomenclature.info(logger, Nomenclature.Action.AUTH);
        // Check if user is blocked
        if (loginAttemptServiceImpl.isBlocked(username)) {
            throw new RuntimeException(Nomenclature.ERR.ACC_BLOCKED_BFORCE_PROTECTION);
        }
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );

            // Success: Reset attempts and return JWT
            loginAttemptServiceImpl.loginSucceeded(username);
            return jwtUtils.generateToken(username);

        } catch (BadCredentialsException e) {
            // 3. Failure: Track attempt and rethrow
            loginAttemptServiceImpl.loginFailed(username);
            throw e;
        }
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return customUserDetailsService.loadUserByUsername(username);
    }

    @Override
    public void changePassword(String username, String oldPassword, String newPassword) {
        Nomenclature.info(logger, Nomenclature.Action.UPDATE_SENSITIVE, username);

        // We check this BEFORE expensive database/auth operations
        if (oldPassword.equals(newPassword)) throw new IllegalArgumentException(Nomenclature.ERR.DETAIL_SAME_PASSWORD);

        // Authenticate the user with the old password
        // This will throw an AuthenticationException if the old password doesn't match
        authenticate(username, oldPassword);

        // Encode the password before saving
        String encodedPassword = passwordEncoder.encode(newPassword);

        // If authentication passed, update the user in the database
        // The newPassword should be encoded via PasswordEncoder
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
