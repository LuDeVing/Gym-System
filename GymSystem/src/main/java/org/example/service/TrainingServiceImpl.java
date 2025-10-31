package org.example.service;

import org.example.DAO.TrainingDao;
import org.example.model.Training;
import org.example.model.TrainingType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class TrainingServiceImpl implements TrainingService {

    private TrainingDao trainingDao;

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public void create(Long traineeId, Long trainerId, String trainingName,
                       String trainingTypeName, LocalDate trainingDate, int trainingDuration) {

        TrainingType trainingType = new TrainingType(trainingTypeName);
        Training training = new Training(traineeId, trainerId, trainingName, trainingType, trainingDate, trainingDuration);

        trainingDao.create(training);
    }

    @Override
    public Training select(Long trainingId) {
        return trainingDao.select(trainingId);
    }

}
