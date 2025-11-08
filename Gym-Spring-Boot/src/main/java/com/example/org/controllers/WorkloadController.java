package com.example.org.controllers;

import com.example.org.exceptions.ForbiddenOperationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/workload")

public class WorkloadController {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadController.class);

    @Autowired
    private TrainingServiceClient trainingServiceClient;

    @GetMapping("/{username}/training-hours")
    @Operation(
            summary = "Get monthly training hours for a trainer (only self)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Training hours retrieved successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", additionalPropertiesSchema = Integer.class)
                            )
                    ),
                    @ApiResponse(responseCode = "403", description = "Forbidden access"),
                    @ApiResponse(responseCode = "404", description = "Trainer not found"),
                    @ApiResponse(responseCode = "500", description = "Failed to retrieve training hours")
            }
    )
    public ResponseEntity<?> getTrainerHours(
            @PathVariable String username,
            @AuthenticationPrincipal Jwt jwt
    ) throws ForbiddenOperationException {

        String transactionId = MDC.get("transactionID");
        logger.info("GET /workload/{}/training-hours called, transactionID={}", username, transactionId);

        if (!username.equals(jwt.getSubject())) {
            logger.warn("User {} tried to access training hours for {}, transactionID={}", jwt.getSubject(), username, transactionId);
            throw new ForbiddenOperationException("You can only access your own training hours");
        }

        try {
            String bearerToken = "Bearer " + jwt.getTokenValue();
            ResponseEntity<?> response = trainingServiceClient.getHours(bearerToken, MDC.get("transactionID"), username);

            if (response.getStatusCode().is2xxSuccessful()) {
                logger.info("Training hours for {} retrieved successfully, transactionID={}", username, transactionId);
                return ResponseEntity.ok(response.getBody());
            } else if (response.getStatusCode().is4xxClientError()) {
                logger.warn("Trainer {} not found in microservice, transactionID={}", username, transactionId);
                return ResponseEntity.status(404).body(Map.of("error", "Trainer not found in training service"));
            } else {
                logger.error("Failed to retrieve training hours for {}, status={}, transactionID={}", username, response.getStatusCode(), transactionId);
                return ResponseEntity.status(500).body(Map.of("error", "Failed to retrieve training hours"));
            }
        } catch (Exception e) {
            logger.error("Exception while retrieving training hours for {}, transactionID={}, error={}", username, transactionId, e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Internal server error"));
        }
    }
}
