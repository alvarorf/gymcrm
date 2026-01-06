package com.gymcrm.dao.interfaces;

import com.gymcrm.model.Trainer;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TrainerDao {
    // Trainer Service class should support possibility to create/update (save)
    // update, select(findById, findAll),  Trainer profile.

    Trainer save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();

    // Finder for username (for requirement 7)
    Optional<Trainer> findByUsername(String username);

}
