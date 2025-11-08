package com.example.org.service;

import com.example.org.data.TrainingRepository;
import com.example.org.model.Training;
import com.example.org.requestBodies.TrainingMicroserviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class TrainingServiceImpl implements TrainingService {

    private final TrainingRepository trainingDao;

    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceImpl.class);

    @Autowired
    private WorkloadProducer workloadProducer;

    @Autowired
    public TrainingServiceImpl(TrainingRepository trainingDao) {
        this.trainingDao = trainingDao;
    }

    @Override
    public Training create(Training training) {

        logger.info("Training microservice called");

        TrainingMicroserviceRequest request = new TrainingMicroserviceRequest();

        request.setTrainerFirstName(training.getTrainer().getFirstName());
        request.setTrainerLastName(training.getTrainer().getLastName());
        request.setTrainerUsername(training.getTrainer().getUsername());
        request.setIsActive(training.getTrainer().isActive());
        request.setTrainingDate(training.getTrainingDate());
        request.setTrainingDuration(training.getTrainingDuration());
        request.setActionType("ADD");

        workloadProducer.sendTrainerWorkload(request);

        return trainingDao.save(training);
    }

    @Override
    public Optional<Training> select(Long trainingId) {
        return trainingDao.findById(trainingId);
    }

}
