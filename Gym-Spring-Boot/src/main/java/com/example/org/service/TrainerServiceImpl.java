package com.example.org.service;


import com.example.org.data.TraineeRepository;
import com.example.org.data.TrainerRepository;
import com.example.org.util.PasswordGenerator;
import com.example.org.util.UserNameCalculator;
import jakarta.transaction.Transactional;
import com.example.org.model.Trainee;
import com.example.org.model.Trainer;
import com.example.org.model.Training;
import com.example.org.model.User;
import com.example.org.util.PasswordGenerator;
import com.example.org.util.UserNameCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@Service
public class TrainerServiceImpl implements TrainerService {

    private static final Logger logger = LoggerFactory.getLogger(TrainerServiceImpl.class);

    private final TraineeRepository traineeDao;
    private final TrainerRepository trainerDao;
    private final UserNameCalculator userNameCalculator;
    private final PasswordGenerator passwordGenerator;

    @Autowired
    public TrainerServiceImpl(TraineeRepository traineeDao,
                              UserNameCalculator userNameCalculator,
                              TrainerRepository trainerDao,
                              PasswordGenerator passwordGenerator) {
        this.traineeDao = traineeDao;
        this.userNameCalculator = userNameCalculator;
        this.passwordGenerator = passwordGenerator;
        this.trainerDao = trainerDao;
    }

    @Override
    @Transactional
    public Trainer create(User user, String specialization) {

        String userName = userNameCalculator.getUserName(user.getFirstName(), user.getLastName());
        String password = passwordGenerator.generateRandomPassword();

        Trainer trainer = new Trainer(user.getFirstName(), user.getLastName(), userName, password,
                user.isActive(), specialization);

        return trainerDao.save(trainer);

    }

    @Override
    public Optional<Trainer> select(Long id) {
        return trainerDao.findById(id);
    }

    @Override
    public void update(Trainer trainer) {
        trainerDao.save(trainer);
    }

    @Override
    public Optional<Trainer> selectByUserName(String username) {
        return trainerDao.findByUsername(username);
    }

    @Override
    public void changePassword(String username, String newPassword) {
        Optional<Trainer> trainerOpt = selectByUserName(username);
        if (trainerOpt.isEmpty()) {
            logger.warn("Cannot change password, trainer with username={} not found", username);
            return;
        }

        Trainer trainer = trainerOpt.get();
        trainer.setPassword(newPassword);
        trainerDao.save(trainer);

        logger.info("Password updated successfully for trainer with username={}", username);
    }

    @Override
    public void deleteByUserName(String username) {
        Optional<Trainer> trainerOpt = selectByUserName(username);
        if (trainerOpt.isEmpty()) {
            logger.warn("Cannot delete, trainer with username={} not found", username);
            return;
        }

        trainerDao.deleteById(trainerOpt.get().getUserId());
        logger.info("Trainer deleted successfully: username={}", username);
    }

    @Override
    public void activate(Long id, boolean activate) {
        Optional<Trainer> trainerOpt = trainerDao.findById(id);
        if (trainerOpt.isEmpty()) {
            logger.warn("Cannot activate/deactivate, trainer with id={} not found", id);
            return;
        }

        Trainer trainer = trainerOpt.get();
        trainer.setActive(activate);
        trainerDao.save(trainer);

        logger.info("Trainer with id={} set active={}", id, activate);
    }

    @Override
    public List<Training> getTrainings(String username, String traineeName, LocalDate from, LocalDate to) {
        Optional<Trainer> trainerOpt = selectByUserName(username);
        if (trainerOpt.isEmpty()) {
            logger.warn("No trainings found, trainer with username={} not found", username);
            return List.of();
        }

        Trainer trainer = trainerOpt.get();
        List<Training> returnTrainings = new ArrayList<>();

        for (Training training : trainer.getTrainings()) {
            Optional<Trainee> traineeOp = traineeDao.findById(training.getTrainee().getUserId());
            if (traineeOp.isEmpty()) continue;

            Trainee trainee = traineeOp.get();

            if (!Objects.equals(trainee.getUsername(), traineeName))
                continue;

            if (!(isWithinRange(training.getTrainingDate(), from, to)))
                continue;

            returnTrainings.add(training);
        }

        return returnTrainings;
    }

    private boolean isWithinRange(LocalDate date, LocalDate from, LocalDate to) {
        return !(date.isBefore(from) || date.isAfter(to));
    }

}
