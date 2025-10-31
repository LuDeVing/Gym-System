package org.example.DAO;

import org.example.model.Trainee;
import org.example.model.Training;
import org.example.storage.StorageSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TrainingDaoImpl implements TrainingDao {

        private final Map<Long, Training> trainings;

        @Autowired
        public TrainingDaoImpl(StorageSystem storage) {
            this.trainings = storage.getTrainings();
        }

        @Override
        public void create(Training training) {
            trainings.put(training.getTrainingId(), training);
        }

        @Override
        public Training select(Long id) {
            return trainings.get(id);
        }

    @Override
    public void delete(Long id) {
        trainings.remove(id);
    }

    public List<Training> findAll() {
        return new ArrayList<>(trainings.values());
    }

}

