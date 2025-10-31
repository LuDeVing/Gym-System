package org.example.service;

import org.example.model.Trainer;
import java.time.LocalDate;

public interface TrainerService {
    void create(String firstName, String lastName, boolean isActive,
                LocalDate dateOfBirth, String specialization);

    Trainer select(Long id);

    void update(Trainer trainer);

}
