package com.example.org.data;

import com.example.org.model.Trainee;
import com.example.org.model.Training;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainingRepository extends JpaRepository<Training, Long> {
}
