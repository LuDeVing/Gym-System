package org.example.service;

import org.example.DAO.TrainerDao;
import org.example.model.Trainer;
import org.example.model.User;
import org.example.util.UserPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class TrainerServiceImpl implements TrainerService {

    private TrainerDao trainerDao;
    private UserPasswordGenerator userPasswordGenerator;
    private long currentId = 0;

    @Autowired
    public void setTrainerDao(TrainerDao trainerDao) {
        this.trainerDao = trainerDao;
    }

    @Autowired
    public void setUserPasswordGenerator(UserPasswordGenerator userPasswordGenerator) {
        this.userPasswordGenerator = userPasswordGenerator;
    }

    @Override
    public void create(String firstName, String lastName, boolean isActive,
                       LocalDate dateOfBirth, String specialization) {

        List<String> allUserNames = trainerDao.findAll().stream().map(User::getUsername).toList();

        String userName = userPasswordGenerator.getUserName(firstName, lastName, allUserNames);
        String password = userPasswordGenerator.generateRandomPassword();

        Trainer trainer = new Trainer(++currentId, firstName, lastName, userName, password, isActive, specialization);

        trainerDao.create(trainer);
    }

    @Override
    public Trainer select(Long id) {
        return trainerDao.select(id);
    }

    @Override
    public void update(Trainer trainer) {
        trainerDao.update(trainer);
    }


}
