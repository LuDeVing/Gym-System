package org.example.service;

import org.example.model.Trainee;

import java.time.LocalDate;

public interface TraineeService {
    void create(String firstName, String lastName, boolean isActive,
                LocalDate dateOfBirth, String address);
    Trainee select(Long Id);
    void update(Trainee trainee);
    void delete(Long Id);
}
