package org.example.util;

import org.example.DAO.TraineeDao;

import java.util.List;

public interface UserPasswordGenerator {

    String getUserName(String firstName, String lastName, List<String> names);
    String generateRandomPassword();
}
