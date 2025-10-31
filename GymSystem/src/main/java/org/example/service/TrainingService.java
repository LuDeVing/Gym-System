package org.example.service;

import org.example.model.Training;

import java.time.LocalDate;

public interface TrainingService {
    void create(Long traineeId, Long trainerId, String trainingName,
                String trainingTypeName, LocalDate trainingDate, int trainingDuration);

    Training select(Long trainingId);
}