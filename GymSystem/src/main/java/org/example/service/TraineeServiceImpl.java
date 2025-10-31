package org.example.service;

import org.example.DAO.TraineeDao;
import org.example.DAO.TrainerDao;
import org.example.DAO.TrainingDao;
import org.example.model.Trainee;
import org.example.model.Training;
import org.example.model.User;
import org.example.util.UserPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

@Service
public class TraineeServiceImpl implements TraineeService {

    private TraineeDao traineeDao;
    private TrainingDao trainingDao;
    private UserPasswordGenerator userPasswordGenerator;
    private long currentId = 0;


    @Autowired
    public void setUserPasswordGenerator(UserPasswordGenerator userPasswordGenerator){
        this.userPasswordGenerator = userPasswordGenerator;
    }

    @Autowired
    public void setTraineeDao(TraineeDao traineeDao) {
        this.traineeDao = traineeDao;
    }

    @Autowired
    public void setTrainingDao(TrainingDao trainingDao) {
        this.trainingDao = trainingDao;
    }


    @Override
    public void create(String firstName, String lastName, boolean isActive,
                       LocalDate dateOfBirth, String address) {

        List<String> allUserNames = traineeDao.findAll().stream().map(User::getUsername).toList();

        String userName = userPasswordGenerator.getUserName(firstName, lastName, allUserNames);
        String password = userPasswordGenerator.generateRandomPassword();

        Trainee trainee = new Trainee(++currentId, firstName, lastName, userName, password, isActive, dateOfBirth, address);

        traineeDao.create(trainee);

    }

    @Override
    public Trainee select(Long Id) {
        return traineeDao.select(Id);
    }

    @Override
    public void update(Trainee trainee) {
        traineeDao.update(trainee);
    }

    @Override
    public void delete(Long Id) {

        for (Training t : trainingDao.findAll()) {
            if (Objects.equals(t.getTraineeId(), Id)) {
                trainingDao.delete(t.getTrainingId());
            }
        }


        traineeDao.delete(Id);

    }
}
