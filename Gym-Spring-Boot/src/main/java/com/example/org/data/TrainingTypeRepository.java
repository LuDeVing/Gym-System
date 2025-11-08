package com.example.org.data;

import com.example.org.model.TrainingType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.Optional;

public interface TrainingTypeRepository extends JpaRepository<TrainingType, String> {
}
