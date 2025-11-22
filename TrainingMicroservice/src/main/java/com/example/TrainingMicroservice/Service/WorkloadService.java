package com.example.TrainingMicroservice.Service;

import com.example.TrainingMicroservice.Stores.TrainerSummaryStore;
import com.example.TrainingMicroservice.dto.TrainingMicroserviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;
import java.util.UUID;

@Service
public class WorkloadService {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadService.class);

    @Autowired
    private TrainerSummaryStore store;

    public void processWorkload(TrainingMicroserviceRequest req, String transactionId, String jwtString) {
        String txId = (transactionId == null || transactionId.isBlank()) ? UUID.randomUUID().toString() : transactionId;
        MDC.put("transactionID", txId);
        try {
            logger.info("Transaction {}: Received workload request (user: {}) action: {} date: {} duration: {}",
                    txId,
                    req != null ? req.getTrainerUsername() : "null",
                    req != null ? req.getActionType() : "null",
                    req != null ? req.getTrainingDate() : "null",
                    req != null ? req.getTrainingDuration() : "null");

            if (req == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Request is required");
            }

            String username = req.getTrainerUsername();
            if (username == null || username.isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Trainer username required");
            }

            if (req.getTrainingDate() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Training date required");
            }

            if (req.getTrainingDuration() == null || req.getTrainingDuration() <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Training duration must be > 0");
            }

            if (req.getActionType() == null || req.getActionType().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Action type required");
            }

            int year = req.getTrainingDate().getYear();
            int month = req.getTrainingDate().getMonthValue();
            int duration = req.getTrainingDuration();

            if ("ADD".equalsIgnoreCase(req.getActionType())) {
                try {
                    store.addDuration(username, req.getTrainerFirstName(), req.getTrainerLastName(), req.getIsActive(), year, month, duration);
                    logger.info("Transaction {}: ADD applied for {} -> {}/{} +{}h", txId, username, year, month, duration);
                } catch (IllegalArgumentException e) {
                    logger.warn("Transaction {}: ADD failed for {}: {}", txId, username, e.getMessage());
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
                } catch (Exception e) {
                    logger.error("Transaction {}: ADD error for {}: {}", txId, username, e.getMessage(), e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add duration");
                }

            } else if ("DELETE".equalsIgnoreCase(req.getActionType())) {
                try {
                    store.deleteDuration(username, year, month, duration);
                    logger.info("Transaction {}: DELETE applied for {} -> {}/{} -{}h", txId, username, year, month, duration);
                } catch (IllegalArgumentException e) {
                    logger.warn("Transaction {}: DELETE failed for {}: {}", txId, username, e.getMessage());
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
                } catch (Exception e) {
                    logger.error("Transaction {}: DELETE error for {}: {}", txId, username, e.getMessage(), e);
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete duration");
                }

            } else {
                logger.warn("Transaction {}: Unknown action type: {}", txId, req.getActionType());
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown action type (use ADD or DELETE)");
            }
        } finally {
            MDC.remove("transactionID");
        }
    }

    public Map<String, Integer> getMonthlyHours(String username, String transactionId, Jwt jwt) {
        String txId = (transactionId == null || transactionId.isBlank()) ? UUID.randomUUID().toString() : transactionId;
        MDC.put("transactionID", txId);
        try {
            if (jwt == null || !username.equals(jwt.getSubject())) {
                logger.warn("Transaction {}: Forbidden access attempt by {} for {}", txId, jwt != null ? jwt.getSubject() : "anonymous", username);
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You can only access your own training hours");
            }

            try {
                Map<String, Integer> result = store.getMonthlyHours(username);
                logger.info("Transaction {}: Returning monthly hours for {}: {}", txId, username, result);
                return result;
            } catch (IllegalArgumentException e) {
                logger.warn("Transaction {}: Trainer not found {}", txId, username);
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage());
            } catch (Exception e) {
                logger.error("Transaction {}: Error fetching hours for {}: {}", txId, username, e.getMessage(), e);
                throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch monthly hours");
            }
        } finally {
            MDC.remove("transactionID");
        }
    }
}
