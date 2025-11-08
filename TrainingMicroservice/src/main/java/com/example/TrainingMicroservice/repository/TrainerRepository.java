package com.example.TrainingMicroservice.repository;

import com.example.TrainingMicroservice.dto.TrainerSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TrainerRepository extends JpaRepository<TrainerSummary, Long> {
    Optional<TrainerSummary> findByUsername(String username);
}
