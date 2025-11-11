package com.example.TrainingMicroservice.controller;

import com.example.TrainingMicroservice.Service.WorkloadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/workload")
public class Controller {

    @Autowired
    private WorkloadService workloadService;

    @GetMapping("/{username}/training-hours")
    public ResponseEntity<?> getTrainerHours(
            @PathVariable("username") String username,
            @RequestHeader(value = "transactionId", required = false) String transactionId,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String txId = (transactionId != null && !transactionId.isBlank()) ? transactionId : UUID.randomUUID().toString();
        Map<String, Integer> hoursMap = workloadService.getMonthlyHours(username, txId, jwt);
        return ResponseEntity.ok(hoursMap);
    }
}
