package org.example.facade;


import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.example.service.TraineeService;
import org.example.service.TrainerService;
import org.example.service.TrainingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class GymFacadeImpl implements GymFacade {

    private final TraineeService traineeService;
    private final TrainerService trainerService;
    private final TrainingService trainingService;

    @Autowired
    public GymFacadeImpl(TraineeService traineeService, TrainerService trainerService, TrainingService trainingService) {
        this.traineeService = traineeService;
        this.trainerService = trainerService;
        this.trainingService = trainingService;
    }

    @Override
    public void createTrainee(String firstName, String lastName, boolean isActive, LocalDate dateOfBirth, String address) {
        traineeService.create(firstName, lastName, isActive, dateOfBirth, address);
    }

    @Override
    public Trainee selectTrainee(Long id) {
        return traineeService.select(id);
    }

    @Override
    public void updateTrainee(Trainee trainee) {
        traineeService.update(trainee);
    }

    @Override
    public void deleteTrainee(Long id) {
        traineeService.delete(id);
    }

    @Override
    public void createTrainer(String firstName, String lastName, boolean isActive, LocalDate dateOfBirth, String specialization) {
        trainerService.create(firstName, lastName, isActive, dateOfBirth, specialization);
    }

    @Override
    public Trainer selectTrainer(Long id) {
        return trainerService.select(id);
    }

    @Override
    public void updateTrainer(Trainer trainer) {
        trainerService.update(trainer);
    }

    @Override
    public void createTraining(Long traineeId, Long trainerId, String trainingName, String trainingTypeName, LocalDate trainingDate, int trainingDuration) {
        trainingService.create(traineeId, trainerId, trainingName, trainingTypeName, trainingDate, trainingDuration);
    }

    @Override
    public Training selectTraining(Long id) {
        return trainingService.select(id);
    }

}