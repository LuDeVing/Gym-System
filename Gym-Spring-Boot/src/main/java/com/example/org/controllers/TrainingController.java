package com.example.org.controllers;

import com.example.org.exceptions.ForbiddenOperationException;
import com.example.org.exceptions.NotFoundException;
import com.example.org.facade.GymFacade;
import com.netflix.appinfo.InstanceInfo;
import com.netflix.discovery.EurekaClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.example.org.requestBodies.*;
import com.example.org.responseBodies.*;
import com.example.org.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;


@RestController
@RequestMapping(value = "/trainings", produces = {"application/json"})
@Tag(name = "Training API", description = "Operations for adding trainings and retrieving training types")
public class TrainingController {

    private static final Logger logger = LoggerFactory.getLogger(TrainingController.class);

    @Autowired
    private GymFacade gymFacade;

    @PostMapping
    @Operation(
            summary = "Add a new training session (trainer can only add trainings for themselves)",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Training added successfully",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "object", additionalPropertiesSchema = String.class))
                    ),
                    @ApiResponse(responseCode = "403", description = "User is not authorized to add this training"),
                    @ApiResponse(responseCode = "404", description = "Trainee or Trainer not found"),
                    @ApiResponse(responseCode = "500", description = "Failed to create training")
            }
    )
    public ResponseEntity<?> addTraining(
            @RequestBody TrainingRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) throws ForbiddenOperationException, NotFoundException {
        String traineeUsername = request.getTraineeUsername();
        String trainerUsername = request.getTrainerUsername();
        String trainingName = request.getTrainingName();
        LocalDate trainingDate = request.getTrainingDate();
        int duration = request.getDuration();

        logger.info("POST /trainings called by {}, transactionID={}", trainerUsername, MDC.get("transactionID"));

        if (!trainerUsername.equals(jwt.getSubject())) {
            logger.warn("User {} tried to add training as {}, transactionID={}", jwt.getSubject(), trainerUsername, MDC.get("transactionID"));
            throw new ForbiddenOperationException("You can only add trainings as yourself");
        }

        Optional<Trainee> te = gymFacade.selectByTraineeName(traineeUsername);
        if (te.isEmpty()) {
            logger.warn("Trainee {} not found, transactionID={}", traineeUsername, MDC.get("transactionID"));
            throw new NotFoundException("Trainee not found");
        }

        Optional<Trainer> tr = gymFacade.selectTrainerByUserName(trainerUsername);
        if (tr.isEmpty()) {
            logger.warn("Trainer {} not found, transactionID={}", trainerUsername, MDC.get("transactionID"));
            throw new NotFoundException("Trainer not found");
        }

        TrainingType tt;
        Optional<TrainingType> existingType = gymFacade.selectTrainingType(trainingName);
        tt = existingType.orElseGet(() -> gymFacade.createTrainingType(new TrainingType(trainingName)));

        Training training = new Training(te.get(), tr.get(), trainingName, tt, trainingDate, duration);

        gymFacade.createTraining(training);
        logger.info("Training '{}' for trainee {} added by trainer {}, transactionID={}", trainingName, traineeUsername, trainerUsername, MDC.get("transactionID"));

        return ResponseEntity.status(201).body(Map.of("message", "Training added successfully"));
    }

    @GetMapping("/types")
    @Operation(
            summary = "Get all available training types",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Returns a list of all training types",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = TrainingTypesResponse.class))
                    )
            }
    )
    public ResponseEntity<TrainingTypesResponse> getTrainingTypes() {
        logger.info("GET /trainings/types called, transactionID={}", MDC.get("transactionID"));
        return ResponseEntity.ok(new TrainingTypesResponse(
                gymFacade.getAllTrainingTypes().stream().map(TrainingType::getTrainingTypeName).toList()
        ));
    }

}
