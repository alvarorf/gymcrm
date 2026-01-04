package com.gymcrm.service.interfaces;

import com.gymcrm.model.Trainee;
import com.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

/*
Trainer Service class should support possibility to create/update/select Trainer profile
 */
public interface TrainerService {
    boolean authenticate(String username, String password);
    Trainer createProfile(Trainer trainer);
    Trainer updateProfile(Trainer trainer, String authUser, String authPass);
    Optional<Trainer> selectProfile(Long id, String authUser, String authPass);
    Optional<Trainer> selectProfile(String username, String authUser, String authPass); // 5. Select Trainer profile by username.
    void updatePassword(Long id, String newPassword, String authUser, String authPass); // 8. Trainer password change
    // Non-idempotent (meaning that executing it multiple times does not necessarily
    // produce the same result) action (Notes: 6)
    void toggleActivation(Long id, String authUser, String authPass);

    List<Trainer> getUnassignedTrainersByTraineeUsername(String traineeUsername, String authUser, String authPass);
}
