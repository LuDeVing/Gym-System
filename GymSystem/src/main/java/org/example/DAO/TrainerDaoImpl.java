package org.example.DAO;

import org.example.model.Trainer;
import org.example.storage.StorageSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TrainerDaoImpl implements TrainerDao {

    private final Map<Long, Trainer> trainers;

    @Autowired
    public TrainerDaoImpl(StorageSystem storage){
        trainers = storage.getTrainers();
    }


    @Override
    public void create(Trainer trainer) {
        trainers.put(trainer.getUserId(), trainer);
    }

    @Override
    public Trainer select(Long Id) {
        return trainers.get(Id);
    }

    @Override
    public void update(Trainer trainer) {
        if (trainers.containsKey(trainer.getUserId())) {
            trainers.put(trainer.getUserId(), trainer);
        } else {
        }
    }

    @Override
    public List<Trainer> findAll() {
        return new ArrayList<>(trainers.values());
    }

}
