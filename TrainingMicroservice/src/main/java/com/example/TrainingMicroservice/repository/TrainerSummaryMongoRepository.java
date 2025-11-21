package com.example.TrainingMicroservice.repository;

import com.example.TrainingMicroservice.dto.TrainerSummaryDocument;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface TrainerSummaryMongoRepository extends MongoRepository<TrainerSummaryDocument, String> {
    Optional<TrainerSummaryDocument> findByUsername(String username);
}
