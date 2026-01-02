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
    void deleteProfile(Long id);
    Optional<Trainee> selectProfile(Long id);
    Optional<Trainee> selectProfile(String username); // 6. Select Trainee profile by username
    boolean authenticate(String username, String password);
    void updatePassword(Long id, String newPassword); // 7. Trainee password change

    // Non-idempotent (meaning that executing it multiple times does not necessarily
    // produce the same result) action (Notes: 6)
    void toggleActivation(Long id);
}
