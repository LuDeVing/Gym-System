package org.example.facade;

import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;

import java.time.LocalDate;

public interface GymFacade {

    void createTrainee(String firstName, String lastName, boolean isActive, LocalDate dateOfBirth, String address);
    Trainee selectTrainee(Long id);
    void updateTrainee(Trainee trainee);
    void deleteTrainee(Long id);

    void createTrainer(String firstName, String lastName, boolean isActive, LocalDate dateOfBirth, String specialization);
    Trainer selectTrainer(Long id);
    void updateTrainer(Trainer trainer);

    void createTraining(Long traineeId, Long trainerId, String trainingName, String trainingTypeName, LocalDate trainingDate, int trainingDuration);
    Training selectTraining(Long id);

}
