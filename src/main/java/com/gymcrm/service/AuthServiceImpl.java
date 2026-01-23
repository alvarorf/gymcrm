package com.gymcrm.service;

import com.gymcrm.dao.interfaces.TraineeDao;
import com.gymcrm.dao.interfaces.TrainerDao;
import com.gymcrm.service.interfaces.AuthService;
import com.gymcrm.util.Nomenclature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final TraineeDao traineeDao;
    private final TrainerDao trainerDao;
    // Logger
    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    public AuthServiceImpl(TraineeDao traineeDao, TrainerDao trainerDao) {
        this.traineeDao = traineeDao;
        this.trainerDao = trainerDao;
    }

    @Override
    public boolean authenticate(String username, String password) {
        Nomenclature.info(logger, Nomenclature.Action.AUTH);
        // Check Trainees first
        boolean isTrainee = traineeDao.findByUsername(username)
                .map(t -> t.getPassword().equals(password)).orElse(false);
        if (isTrainee) return true;

        // Then check Trainers
        return trainerDao.findByUsername(username)
                .map(t -> t.getPassword().equals(password)).orElse(false);
    }
}
