package org.example.DAO;

import org.example.model.Trainee;
import org.example.storage.StorageSystem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Repository
public class TraineeDaoImpl implements TraineeDao {

    private final Map<Long, Trainee> trainees;

    @Autowired
    public TraineeDaoImpl(StorageSystem storageSystem) {
        this.trainees = storageSystem.getTrainees();
    }


    @Override
    public void create(Trainee trainee) {
        trainees.put(trainee.getUserId(), trainee);
    }

    @Override
    public Trainee select(Long Id) {
        return trainees.get(Id);
    }

    @Override
    public void update(Trainee trainee) {
        if(trainees.containsKey(trainee.getUserId())){
            trainees.put(trainee.getUserId(), trainee);
        }
        else{

        }
    }

    @Override
    public void delete(Long Id) {
        if (trainees.containsKey(Id)) {
            trainees.remove(Id);
        } else {

        }
    }

    public List<Trainee> findAll() {
        return new ArrayList<>(trainees.values());
    }

}
