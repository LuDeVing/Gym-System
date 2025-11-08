package com.example.org.controllers;

import com.example.org.requestBodies.TrainingMicroserviceRequest;
import com.example.org.util.TrainingServiceFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "training-microservice",
        fallback = TrainingServiceFallback.class
)
public interface TrainingServiceClient {

    @PostMapping("/workload/")
    ResponseEntity<Void> addTrainer(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader(value = "transactionId", required = false) String transactionId,
            @RequestBody TrainingMicroserviceRequest request
    );

    @GetMapping("/workload/{username}/training-hours")
    ResponseEntity<?> getHours(
            @RequestHeader("Authorization") String bearerToken,
            @RequestHeader(value = "transactionId", required = false) String transactionId,
            @PathVariable("username") String username
    );
}
