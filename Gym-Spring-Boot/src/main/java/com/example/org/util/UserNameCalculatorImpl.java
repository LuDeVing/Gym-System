package com.example.org.util;

import com.example.org.data.TraineeRepository;
import com.example.org.data.TrainerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class UserNameCalculatorImpl implements UserNameCalculator {

    @Autowired
    private TraineeRepository traineeDao;

    @Autowired
    private TrainerRepository trainerDao;

    public String getUserName(String firstName, String lastName) {
        String userName = firstName + "." + lastName;
        int id = calculateUserNumber(userName);
        if (id != 0) {
            userName += id;
        }
        return userName;
    }

    private boolean userNameInDaos(String username) {
        return traineeDao.findByUsername(username).isPresent() ||
                trainerDao.findByUsername(username).isPresent();
    }

    private int calculateUserNumber(String username) {
        int counter = 0;
        String uniqueName = username;
        while (userNameInDaos(uniqueName)) {
            counter++;
            uniqueName = username + counter;
        }
        return counter;
    }
}
