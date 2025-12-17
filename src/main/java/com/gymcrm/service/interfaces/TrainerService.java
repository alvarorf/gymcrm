package com.gymcrm.service.interfaces;

import com.gymcrm.model.Trainer;
import java.util.Optional;

/*
Trainer Service class should support possibility to create/update/select Trainer profile
 */
public interface TrainerService {
    Trainer createProfile(Trainer trainer);
    Trainer updateProfile(Trainer trainer);
    Optional<Trainer> selectProfile(Long id);
}
