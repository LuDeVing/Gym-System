package org.example.DAO;

import org.example.model.Trainee;

import java.util.List;

public interface TraineeDao {
    void create(Trainee trainee);
    Trainee select(Long Id);
    void update(Trainee trainee);
    void delete(Long Id);
    List<Trainee> findAll();
}
