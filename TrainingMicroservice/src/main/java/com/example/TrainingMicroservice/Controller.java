package com.example.TrainingMicroservice;

import com.example.TrainingMicroservice.data.TrainerRepository;
import com.example.TrainingMicroservice.data.TrainerRequest;
import com.example.TrainingMicroservice.data.TrainerSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
public class Controller {

    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

    @Autowired
    private TrainerRepository repository;

    @PostMapping("/workload/")
    public ResponseEntity<HttpStatus> addTrainer(
            @RequestBody TrainerRequest trainer,
            @AuthenticationPrincipal Jwt jwt
    ) {

        String transactionId = trainer.getTransactionId();

        logger.info("Transaction {}: Received workload update request from user: {}", transactionId, jwt.getSubject());
        logger.info("Transaction {}: Trainer username: {}, Action: {}, Date: {}, Duration: {}",
                transactionId,
                trainer.getTrainerUsername(),
                trainer.getActionType(),
                trainer.getTrainingDate(),
                trainer.getTrainingDuration());

        TrainerSummary summary = repository.findByUsername(trainer.getTrainerUsername()).orElseGet(() -> {
            logger.info("Transaction {}: Trainer {} not found, creating new summary entry.", transactionId, trainer.getTrainerUsername());
            return new TrainerSummary(
                    trainer.getTrainerUsername(),
                    trainer.getTrainerFirstName(),
                    trainer.getTrainerLastName(),
                    trainer.getIsActive()
            );
        });

        if ("ADD".equalsIgnoreCase(trainer.getActionType())) {
            logger.info("Transaction {}: Adding training duration for {} in {}/{}",
                    transactionId,
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
                    transactionId,
                    trainer.getTrainerUsername(),
                    trainer.getTrainingDate().getMonthValue(),
                    trainer.getTrainingDate().getYear());
            summary.deleteDuration(
                    trainer.getTrainingDate().getYear(),
                    trainer.getTrainingDate().getMonthValue(),
                    trainer.getTrainingDuration()
            );
        } else {
            logger.warn("Transaction {}: Unknown action type: {}. Skipping update.", transactionId, trainer.getActionType());
            return ResponseEntity.badRequest().build();
        }

        repository.save(summary);
        logger.info("Transaction {}: Trainer summary successfully updated for {}", transactionId, trainer.getTrainerUsername());

        return ResponseEntity.ok().build();
    }
    @GetMapping("/TrainingHours/")
    public ResponseEntity<?> getHours(
            @RequestParam("username") String username,
            @AuthenticationPrincipal Jwt jwt
    ) {
        if (!username.equals(jwt.getSubject())) {
            logger.warn("User {} tried to access training hours for {}, forbidden", jwt.getSubject(), username);
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only access your own training hours");
        }

        Optional<TrainerSummary> optionalSummary = repository.findByUsername(username);

        if (optionalSummary.isEmpty()) {
            logger.warn("Trainer not found: {}", username);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Trainer not found");
        }

        TrainerSummary summary = optionalSummary.get();
        Map<String, Integer> hoursMap = new HashMap<>();
        summary.getMonthlyHours().forEach((year, months) ->
                months.forEach((month, hours) ->
                        hoursMap.put(year + "-" + month, hours)
                )
        );

        logger.info("Returning training hours for trainer {}: {}", username, hoursMap);
        return ResponseEntity.ok(hoursMap);
    }


}
