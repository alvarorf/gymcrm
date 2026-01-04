package com.gymcrm.dao.interfaces;

import com.gymcrm.model.Trainee;
import java.util.List;
import java.util.Optional;

public interface TraineeDao {

    // Create/Update (save), Select or Read (findById, findAll), Delete (delete) operations

    /*
    All reference types (objects, strings, etc.) are nullable, while primitive types
    (like int, boolean, double) are non-nullable. So, Trainee can be null by default.
     */
    Trainee save(Trainee trainee);
    Optional<Trainee> findById(Long id);
    List<Trainee> findAll();
    void delete(Long id);
    void delete(String username);

    // Finder for username (for requirement 7)
    Optional<Trainee> findByUsername(String username);


}
