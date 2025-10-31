package org.example.storage;

import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class StorageSystem {

    private final Map<Long, Trainer> trainers;
    private final Map<Long, Trainee> trainees;
    private final Map<Long, Training> trainings;

    public StorageSystem(){
        trainers = new HashMap<>();
        trainees = new HashMap<>();
        trainings = new HashMap<>();
    }


    public Map<Long, Trainer> getTrainers() {
        return trainers;
    }

    public Map<Long, Trainee> getTrainees() {
        return trainees;
    }

    public Map<Long, Training> getTrainings() {
        return trainings;
    }

}
