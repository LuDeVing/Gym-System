package com.example.TrainingMicroservice.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<TrainerSummary, Long> {
    Optional<TrainerSummary> findByUsername(String username);
}
