package com.example.TrainingMicroservice.Service;

import com.example.TrainingMicroservice.dto.TrainingMicroserviceRequest;
import com.example.TrainingMicroservice.dto.TrainerSummary;
import com.example.TrainingMicroservice.repository.TrainerRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
public class WorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadService.class);

    private final TrainerRepository repository;

    public WorkloadService(TrainerRepository repository) {
        this.repository = repository;
    }

    public void processWorkload(TrainingMicroserviceRequest trainer, String transactionId, String jwt) {
        String txId = transactionId;
        if (txId == null || txId.isBlank()) {
            txId = UUID.randomUUID().toString();
        }

        MDC.put("transactionID", txId);
        try {
            logger.info("Transaction {}: Received workload update request from user: {}", txId, jwt != null ? jwt : "anonymous");
            logger.info("Transaction {}: Trainer username: {}, Action: {}, Date: {}, Duration: {}",
                    txId,
                    trainer.getTrainerUsername(),
                    trainer.getActionType(),
                    trainer.getTrainingDate(),
                    trainer.getTrainingDuration());

            final String txIdFinal = txId;

            TrainerSummary summary = repository.findByUsername(trainer.getTrainerUsername()).orElseGet(() -> {
                logger.info("Transaction {}: Trainer {} not found, creating new summary entry.", txIdFinal, trainer.getTrainerUsername());
                return new TrainerSummary(
                        trainer.getTrainerUsername(),
                        trainer.getTrainerFirstName(),
                        trainer.getTrainerLastName(),
                        trainer.getIsActive()
                );
            });

            if ("ADD".equalsIgnoreCase(trainer.getActionType())) {
                logger.info("Transaction {}: Adding training duration for {} in {}/{}",
                        txId,
                        trainer.getTrainerUsername(),
                        trainer.getTrainingDate().getMonthValue(),
                        trainer.getTrainingDate().getYear());
                summary.addDuration(
                        trainer.getTrainingDate().getYear(),
                        trainer.getTrainingDate().getMonthValue(),
                        trainer.getTrainingDuration()
                );
            } else if ("DELETE".equalsIgnoreCase(trainer.getActionType())) {
                logger.info("Transaction {}: Deleting training duration for {} in {}/{}",
                        txId,
                        trainer.getTrainerUsername(),
                        trainer.getTrainingDate().getMonthValue(),
                        trainer.getTrainingDate().getYear());
                summary.deleteDuration(
                        trainer.getTrainingDate().getYear(),
                        trainer.getTrainingDate().getMonthValue(),
                        trainer.getTrainingDuration()
                );
            } else {
                logger.warn("Transaction {}: Unknown action type: {}. Skipping update.", txId, trainer.getActionType());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action type");
            }

            repository.save(summary);
            logger.info("Transaction {}: Trainer summary successfully updated for {}", txId, trainer.getTrainerUsername());
        } finally {
            MDC.remove("transactionID");
        }
    }

    public Map<String, Integer> getMonthlyHours(String username, String transactionId, Jwt jwt) {
        String txId = (transactionId != null && !transactionId.isBlank()) ? transactionId : UUID.randomUUID().toString();
        MDC.put("transactionID", txId);
        try {
            if (jwt == null || !username.equals(jwt.getSubject())) {
                logger.warn("Transaction {}: User {} tried to access training hours for {}, forbidden", txId, jwt != null ? jwt.getSubject() : "anonymous", username);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own training hours");
            }

            Optional<TrainerSummary> optionalSummary = repository.findByUsername(username);
            if (optionalSummary.isEmpty()) {
                logger.warn("Transaction {}: Trainer not found: {}", txId, username);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found");
            }

            TrainerSummary summary = optionalSummary.get();
            Map<String, Integer> hoursMap = new HashMap<>();
            summary.getMonthlyHours().forEach((year, months) -> {
                months.forEach((month, hours) -> {
                    hoursMap.put(year + "-" + month, hours);
                });
            });

            logger.info("Transaction {}: Returning training hours for trainer {}: {}", txId, username, hoursMap);
            return hoursMap;
        } finally {
            MDC.remove("transactionID");
        }
    }
}
