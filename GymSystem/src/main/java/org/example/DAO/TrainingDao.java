package org.example.DAO;

import org.example.model.Training;
import java.util.List;

public interface TrainingDao {
    void create(Training training);
    Training select(Long id);
    void delete(Long id);
    List<Training> findAll();
}
