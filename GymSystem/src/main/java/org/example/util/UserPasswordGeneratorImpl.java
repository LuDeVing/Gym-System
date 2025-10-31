package org.example.util;

import org.example.DAO.TraineeDao;
import org.example.model.Trainee;

import java.security.SecureRandom;
import java.util.List;

public class UserPasswordGeneratorImpl implements UserPasswordGenerator {

    private static final String CHARACTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int LENGTH = 10;
    private static final SecureRandom random = new SecureRandom();

    private int calculateUserNumber(String username, List<String> names) {

        int id = 0;

        for (String name: names) {

            String regex = "^" + username + "(\\d*)$";

            if (name.matches(regex)){
                id += 1;
            }

        }

        return id;

    }

    public String getUserName(String firstName, String lastName, List<String> names){

        String userName = firstName + "." + lastName;
        int id = calculateUserNumber(userName, names);
        if (id != 0){
            userName += id;
        }

        return userName;

    }

    public String generateRandomPassword() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }

}
