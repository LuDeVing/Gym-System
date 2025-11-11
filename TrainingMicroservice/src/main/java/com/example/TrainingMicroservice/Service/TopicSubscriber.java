package com.example.TrainingMicroservice.Service;

import com.example.TrainingMicroservice.dto.TrainingMicroserviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

@Service
public class TopicSubscriber {

    private static final Logger logger = LoggerFactory.getLogger(TopicSubscriber.class);

    @Autowired
    private JwtDecoder jwtDecoder;

    @Autowired
    private WorkloadService workloadService;

    @JmsListener(destination = "trainings.topic", containerFactory = "topicListenerFactory")
    public void receiveMessage(TrainingMicroserviceRequest request, @Headers Map<String, Object> headers) {
        String authHeader = headers.get("Authorization") != null ? headers.get("Authorization").toString() : null;
        String token = authHeader != null && authHeader.startsWith("Bearer ") ? authHeader.substring(7) : null;


        if (token != null && validateToken(token) && request.getTrainerUsername() != null && request.getTrainingDate() != null) {
            String transactionId = headers.get("TransactionId") != null ? headers.get("TransactionId").toString() : null;
            workloadService.processWorkload(request, transactionId, token);

        } else {
            logger.warn("Invalid or missing token, ignoring message for trainer: {}", request.getTrainerUsername());
        }
    }

    public boolean validateToken(String token) {
        try {
            Jwt jwt = jwtDecoder.decode(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }
}
