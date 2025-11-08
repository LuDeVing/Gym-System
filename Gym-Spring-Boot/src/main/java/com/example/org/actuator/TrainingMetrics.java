package com.example.org.actuator;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;

public class TrainingMetrics {

    private final Timer createTrainingTimer;

    public TrainingMetrics(MeterRegistry registry) {
        this.createTrainingTimer = Timer.builder("gym.training.create.time")
                .description("Time taken to create a training")
                .register(registry);
    }

}
