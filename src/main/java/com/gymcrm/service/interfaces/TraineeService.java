package com.gymcrm.service.interfaces;

import com.gymcrm.model.Trainee;

import java.util.List;
import java.util.Optional;
/*
Trainee Service class should support possibility to create/update/delete/select Trainee
profile.
*/
public interface TraineeService {
    Trainee createProfile(Trainee trainee);
    Trainee updateProfile(Trainee trainee);
    void deleteProfile(Long id);
    void deleteProfile(String targetUser); // 13. Delete trainee profile by username.
    Optional<Trainee> selectProfile(Long id);
    Optional<Trainee> selectTraineeProfile(String targetUser); // 6. Select Trainee profile by username
    void updatePassword(Long id, String newPassword); // 7. Trainee password change

    // Non-idempotent (meaning that executing it multiple times does not necessarily
    // produce the same result) action (Notes: 6)
    void toggleActivation(Long id);

    // 18. Update Trainee's trainers list
    void updateTraineeTrainers(String traineeUsername, List<String> trainerUsernames);
}