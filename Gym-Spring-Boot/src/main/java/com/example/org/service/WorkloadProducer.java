package com.example.org.service;

import com.example.org.requestBodies.TrainingMicroserviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class WorkloadProducer {

    private static final Logger logger = LoggerFactory.getLogger(WorkloadProducer.class);

    private final TopicProducer topicProducer;

    @Autowired
    public WorkloadProducer(TopicProducer topicProducer) {
        this.topicProducer = topicProducer;
    }

    public void sendTrainerWorkload(TrainingMicroserviceRequest request) {
        try {
            String transactionId = MDC.get("transactionId");
            String bearerToken = extractJwtTokenValue();

            topicProducer.publishTrainingMicroService(request, transactionId, bearerToken);

            logger.info("Published training workload to topic for trainer: {}", request.getTrainerUsername());
        } catch (Exception e) {
            logger.error("Failed to publish training workload", e);
        }
    }

    private String extractJwtTokenValue() {
        var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken jwtAuth) {
            var jwt = jwtAuth.getToken();
            return jwt != null ? jwt.getTokenValue() : null;
        }
        if (auth != null && auth.getPrincipal() instanceof org.springframework.security.oauth2.jwt.Jwt jwt) {
            return jwt.getTokenValue();
        }
        logger.warn("No JWT found in SecurityContext");
        return null;
    }
}
