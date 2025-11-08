package com.example.org.data;

import com.example.org.model.Trainer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    List<Trainer> findByUserIdNotIn(List<Long> ids);
    Optional<Trainer> findByUsername(String username);
}
