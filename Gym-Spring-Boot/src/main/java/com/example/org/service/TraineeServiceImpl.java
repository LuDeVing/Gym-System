package com.example.org.service;

import com.example.org.data.TraineeRepository;
import com.example.org.data.TrainerRepository;
import com.example.org.data.TrainingRepository;
import com.example.org.model.Trainee;
import com.example.org.model.Trainer;
import com.example.org.model.Training;
import com.example.org.util.PasswordGenerator;
import com.example.org.util.UserNameCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.example.org.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class TraineeServiceImpl implements TraineeService {

    private static final Logger logger = LoggerFactory.getLogger(TraineeServiceImpl.class);

    private final TraineeRepository traineeDao;
    private final TrainerRepository trainerDao;
    private final UserNameCalculator userNameCalculator;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    public TraineeServiceImpl(TraineeRepository traineeDao,
                              UserNameCalculator userNameCalculator,
                              TrainerRepository trainerDao,
                              PasswordGenerator passwordGenerator) {
        this.traineeDao = traineeDao;
        this.userNameCalculator = userNameCalculator;
        this.passwordGenerator = passwordGenerator;
        this.trainerDao = trainerDao;
    }

    @Override
    public Trainee create(User user, LocalDate date, String address) {
        String userName = userNameCalculator.getUserName(user.getFirstName(), user.getLastName());
        String password = passwordGenerator.generateRandomPassword();
        Trainee trainee = new Trainee(user.getFirstName(), user.getLastName(), userName, password,
                user.isActive(), date, address);
        return traineeDao.save(trainee);
    }

    @Override
    public Optional<Trainee> select(Long id) {
        return traineeDao.findById(id);
    }

    @Override
    public void update(Trainee trainee) {
        traineeDao.save(trainee);
    }

    @Override
    public void delete(Long id) {
        traineeDao.deleteById(id);
        logger.info("Deleted Trainee with ID: {}", id);
    }

    @Override
    public Optional<Trainee> selectByUserName(String username) {
        return traineeDao.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String newPassword) {
        Optional<Trainee> traineeOpt = traineeDao.findByUsername(username);
        if (traineeOpt.isEmpty()) {
            logger.warn("Cannot change password, trainee with username={} not found", username);
            return;
        }
        Trainee trainee = traineeOpt.get();
        trainee.setPassword(newPassword);
        traineeDao.save(trainee);
        logger.info("Password updated successfully for trainee with username={}", username);
    }

    public void deleteByUserName(String username) {
        Optional<Trainee> traineeOpt = traineeDao.findByUsername(username);
        if (traineeOpt.isEmpty()) {
            logger.warn("Cannot delete, trainee with username={} not found", username);
            return;
        }
        traineeDao.deleteById(traineeOpt.get().getUserId());
        logger.info("Trainee deleted successfully: username={}", username);
    }

    public void activate(Long id, boolean activate) {
        Optional<Trainee> traineeOpt = traineeDao.findById(id);
        if (traineeOpt.isEmpty()) {
            logger.warn("Cannot activate/deactivate, trainee with id={} not found", id);
            return;
        }
        Trainee trainee = traineeOpt.get();
        trainee.setActive(activate);
        traineeDao.save(trainee);
        logger.info("Trainee with id={} set active={}", id, activate);
    }

    public List<Training> getTrainings(String username, String trainerName, LocalDate from, LocalDate to) {
        Optional<Trainee> traineeOpt = traineeDao.findByUsername(username);
        if (traineeOpt.isEmpty()) {
            logger.warn("No trainings found, trainee with username={} not found", username);
            return List.of();
        }
        Trainee trainee = traineeOpt.get();
        List<Training> returnTrainings = new ArrayList<>();
        for (Training training : trainee.getTrainings()) {
            Optional<Trainer> trainerOp = trainerDao.findById(training.getTrainer().getUserId());
            if (trainerOp.isEmpty()) continue;
            Trainer trainer = trainerOp.get();
            if (!Objects.equals(trainer.getUsername(), trainerName))
                continue;
            if (!(isWithinRange(training.getTrainingDate(), from, to)))
                continue;
            returnTrainings.add(training);
        }
        return returnTrainings;
    }

    @Override
    public List<Trainer> getUnsignedTrainers(String traineeUsername) {
        Optional<Trainee> traineeOpt = traineeDao.findByUsername(traineeUsername);
        if (traineeOpt.isEmpty()) {
            logger.warn("Trainee with username={} not found", traineeUsername);
            return List.of();
        }
        Trainee trainee = traineeOpt.get();
        List<Long> assignedTrainerIds = trainee.getTrainings().stream()
                .map(training -> training.getTrainer().getUserId())
                .toList();
        if (assignedTrainerIds.isEmpty()) {
            return trainerDao.findAll();
        } else {
            return trainerDao.findByUserIdNotIn(assignedTrainerIds);
        }
    }

    private boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        return !(date.isBefore(from) || date.isAfter(to));
    }
}
