package com.gymcrm.service.interfaces;

import com.gymcrm.model.Trainee;
import java.util.Optional;
/*
Trainee Service class should support possibility to create/update/delete/select Trainee
profile.
*/
public interface TraineeService {
    Trainee createProfile(Trainee trainee);
    Trainee updateProfile(Trainee trainee);
    Optional<Trainee> selectProfile(Long id);
    void deleteProfile(Long id);
}
