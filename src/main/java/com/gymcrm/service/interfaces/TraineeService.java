package com.gymcrm.service.interfaces;

import com.gymcrm.dto.RegistrationResponse;
import com.gymcrm.dto.TraineeProfileResponse;
import com.gymcrm.dto.TraineeRegistrationRequest;
import com.gymcrm.dto.TraineeUpdateRequest;

import java.util.List;
import java.util.Optional;
/*
Trainee Service class should support possibility to create/update/delete/select Trainee
profile.
*/
public interface TraineeService {
    RegistrationResponse createProfile(TraineeRegistrationRequest trainee);
    TraineeProfileResponse updateProfile(TraineeUpdateRequest trainee);
    void deleteProfile(Long id);
    void deleteProfile(String targetUser); // 13. Delete trainee profile by username.
    Optional<TraineeProfileResponse> selectProfile(Long id);
    Optional<TraineeProfileResponse> selectTraineeProfile(String targetUser); // 6. Select Trainee profile by username
    void updatePassword(Long id, String newPassword); // 7. Trainee password change

    // Non-idempotent (meaning that executing it multiple times does not necessarily
    // produce the same result) action (Notes: 6)
    void toggleActivation(Long id);
    void toggleActivation(String username);

    // 18. Update Trainee's trainers list
    void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames);
}