package com.example.TrainingMicroservice.Service;

import com.example.TrainingMicroservice.dto.TrainerSummaryDocument;
import com.example.TrainingMicroservice.dto.TrainingMicroserviceRequest;
import com.example.TrainingMicroservice.repository.TrainerSummaryMongoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
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

    @Autowired
    private TrainerSummaryMongoRepository repository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public void processWorkload(TrainingMicroserviceRequest req, String transactionId, String jwtString) {
        String txId = (transactionId == null || transactionId.isBlank()) ? UUID.randomUUID().toString() : transactionId;
        MDC.put("transactionID", txId);
        try {
            logger.info("Transaction {}: Received workload request (user: {}) action: {} date: {} duration: {}",
                    txId,
                    req.getTrainerUsername(),
                    req.getActionType(),
                    req.getTrainingDate(),
                    req.getTrainingDuration());

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
                Query query = Query.query(Criteria.where("username").is(username));
                String fieldPath = String.format("years.%d.%d", year, month);
                Update update = new Update()
                        .inc(fieldPath, duration)
                        .setOnInsert("username", username)
                        .setOnInsert("trainerFirstName", req.getTrainerFirstName())
                        .setOnInsert("trainerLastName", req.getTrainerLastName())
                        .setOnInsert("isActive", req.getIsActive());
                FindAndModifyOptions options = FindAndModifyOptions.options()
                        .upsert(true)
                        .returnNew(true);
                TrainerSummaryDocument updated = mongoTemplate.findAndModify(query, update, options, TrainerSummaryDocument.class);
                logger.info("Transaction {}: ADD applied for {} -> field {} incremented by {}. Result doc id={}",
                        txId, username, fieldPath, duration, updated != null ? updated.getId() : "null");

            } else if ("DELETE".equalsIgnoreCase(req.getActionType())) {
                Optional<TrainerSummaryDocument> opt = repository.findByUsername(username);
                if (opt.isEmpty()) {
                    logger.warn("Transaction {}: Trainer {} not found for DELETE", txId, username);
                    throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found");
                }

                TrainerSummaryDocument doc = opt.get();
                Map<String, Map<String, Integer>> years = doc.getYears() != null ? doc.getYears() : new HashMap<>();
                String yearKey = String.valueOf(year);
                String monthKey = String.valueOf(month);
                int current = 0;
                if (years.containsKey(yearKey) && years.get(yearKey) != null) {
                    current = years.get(yearKey).getOrDefault(monthKey, 0);
                }

                int newVal = Math.max(0, current - duration);
                String fieldPath = String.format("years.%s.%s", yearKey, monthKey);

                Query query = Query.query(Criteria.where("username").is(username));
                Update update = new Update().set(fieldPath, newVal);
                mongoTemplate.updateFirst(query, update, TrainerSummaryDocument.class);

                logger.info("Transaction {}: DELETE applied for {} -> {} (was {})", txId, username, newVal, current);

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

            TrainerSummaryDocument doc = repository.findByUsername(username)
                    .orElseThrow(() -> {
                        logger.warn("Transaction {}: Trainer not found {}", txId, username);
                        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Trainer not found");
                    });

            Map<String, Integer> result = new HashMap<>();
            if (doc.getYears() != null) {
                doc.getYears().forEach((year, months) -> {
                    if (months != null) {
                        months.forEach((month, hours) -> {
                            result.put(year + "-" + month, hours);
                        });
                    }
                });
            }

            logger.info("Transaction {}: Returning monthly hours for {}: {}", txId, username, result);
            return result;
        } finally {
            MDC.remove("transactionID");
        }
    }
}
