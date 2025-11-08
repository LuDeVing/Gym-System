package com.example.org.util;

import com.example.org.requestBodies.TrainerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;

public class TrainingServiceFallback {
    private static final Logger logger = LoggerFactory.getLogger(TrainingServiceFallback.class);

    public ResponseEntity<Void> addTrainer(String bearerToken, TrainerRequest request) {
        logger.error("TrainingServiceFallback triggered — failed to call training-microservice for trainer '{}'",
                request.getTrainerUsername());

        return ResponseEntity.status(503).build();
    }
}
