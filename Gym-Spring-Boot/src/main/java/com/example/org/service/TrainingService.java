package com.example.org.service;

import com.example.org.model.Training;

import java.util.Optional;

public interface TrainingService {
    Training create(Training training);

    Optional<Training> select(Long trainingId);

}