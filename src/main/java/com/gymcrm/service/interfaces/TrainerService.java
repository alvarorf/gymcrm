package com.gymcrm.service.interfaces;

import com.gymcrm.dto.*;

import java.util.List;
import java.util.Optional;

/*
Trainer Service class should support possibility to create/update/select Trainer profile
 */
public interface TrainerService {
    RegistrationResponse createProfile(TrainerRegistrationRequest trainer);
    TrainerProfileResponse updateProfile(TrainerUpdateRequest trainer);
    Optional<TrainerProfileResponse> selectTrainerProfile(Long id);
    Optional<TrainerProfileResponse> selectTrainerProfile(String username); // 5. Select Trainer profile by username.
    void updatePassword(Long id, String newPassword); // 8. Trainer password change
    // Non-idempotent (meaning that executing it multiple times does not necessarily
    // produce the same result) action (Notes: 6)
    void toggleActivation(Long id);
    void toggleActivation(String username);

    List<TrainerShortResponse> getUnassignedActiveTrainersByTraineeUsername(String traineeUsername);
}
