package org.example.DAO;

import org.example.model.Trainer;

import java.util.Arrays;
import java.util.List;

public interface TrainerDao {

    void create(Trainer trainer);
    Trainer select(Long Id);
    void update(Trainer trainer);
    List<Trainer> findAll();
}
