package com.gymcrm.dao.interfaces;

import com.gymcrm.model.Trainer;

import java.util.List;
import java.util.Optional;

public interface TrainerDao {
    // Trainer Service class should support possibility to create (save)
    // update, select(findById, findAll),  Trainer profile.

    Trainer save(Trainer trainer);
    Optional<Trainer> findById(Long id);
    List<Trainer> findAll();

    // Finder for username (for requirement 7)
    Optional<Trainer> findByUsername(String username);

}
