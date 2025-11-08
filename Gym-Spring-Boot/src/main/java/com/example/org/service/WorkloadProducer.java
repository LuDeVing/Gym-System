package com.example.org.service;

import com.example.org.controllers.TrainingServiceClient;
import com.example.org.requestBodies.TrainingMicroserviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.slf4j.MDC;

@Service
public class WorkloadProducer {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadProducer.class);

    @Autowired
    private TrainingServiceClient trainingServiceClient;

    public void sendTrainerWorkload(TrainingMicroserviceRequest request) {
        try {
            String tokenValue = extractJwtTokenValue();
            String authHeader = (tokenValue != null) ? "Bearer " + tokenValue : null;

            String transactionId = MDC.get("transactionId"); // assuming you set it earlier per request
            ResponseEntity<Void> response = trainingServiceClient.addTrainer(authHeader, transactionId, request);

            logger.info("Training microservice response: {}", response.getStatusCode());
        } catch (Exception e) {
            logger.error("Failed to call training microservice", e);
        }
    }

    private String extractJwtTokenValue() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            return jwt != null ? jwt.getTokenValue() : null;
        }
        if (auth != null && auth.getPrincipal() instanceof Jwt jwt) {
            return jwt.getTokenValue();
        }
        logger.warn("No JWT found in SecurityContext");
        return null;
    }
}
