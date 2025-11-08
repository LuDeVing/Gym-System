package com.example.org.service;

import com.example.org.model.Trainer;
import com.example.org.model.Training;
import com.example.org.model.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TrainerService {
    Trainer create(User user, String specialization);
    Optional<Trainer> select(Long id);
    void update(Trainer trainer);

    Optional<Trainer> selectByUserName(String username);
    void changePassword(String password, String newPassword);
    void deleteByUserName(String username);
    void activate(Long id, boolean activate);

    List<Training> getTrainings(String username, String TraineeName, LocalDate from, LocalDate to);


}
