package com.example.org.service;

import com.example.org.requestBodies.TrainingMicroserviceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessagePostProcessor;
import org.springframework.stereotype.Service;

@Service
public class TopicProducer {

    private static final Logger logger = LoggerFactory.getLogger(TopicProducer.class);
    private static final String TOPIC_NAME = "trainings.topic";

    private final JmsTemplate jmsTemplate;

    public TopicProducer(JmsTemplate jmsTemplate) {
        this.jmsTemplate = jmsTemplate;
        this.jmsTemplate.setPubSubDomain(true);
    }

    public void publishTrainingMicroService(TrainingMicroserviceRequest request,
                                            String transactionId,
                                            String bearerToken) {
        try {
            jmsTemplate.convertAndSend(TOPIC_NAME, request, message -> {
                if (transactionId != null) message.setStringProperty("TransactionId", transactionId);
                if (bearerToken != null) message.setStringProperty("Authorization", "Bearer " + bearerToken);
                return message;
            });

            logger.info("Successfully published message to topic '{}': {}", TOPIC_NAME, request);
            logger.debug("Headers -> TransactionId: {}, Authorization: Bearer {}", transactionId, bearerToken);
        } catch (Exception e) {
            logger.error("Failed to publish message to topic '{}': {}", TOPIC_NAME, e.getMessage(), e);
        }
    }
}

