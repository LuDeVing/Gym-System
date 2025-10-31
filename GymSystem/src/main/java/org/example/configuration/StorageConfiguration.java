package org.example.configuration;

import jakarta.annotation.PostConstruct;
import org.example.model.Trainee;
import org.example.model.Trainer;
import org.example.model.Training;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ComponentScan(basePackages = "org.example.storage")
public class StorageConfiguration {

    private Map<Long, Trainee> traineeMap;
    private Map<Long, Training> trainingMap;
    private Map<Long, Trainer> trainerMap;

    @Value("%{storage.file}")
    String storageFilePath;

    @Bean
    public Map<Long, Trainee> getTraineeMap(){
        return traineeMap;
    }

    @Bean
    public Map<Long, Trainer> getTrainerMap(){
        return trainerMap;
    }

    @Bean
    public Map<Long, Training> getTrainingMap(){
        return trainingMap;
    }

    @PostConstruct
    public void getData(){

        File file = new File(storageFilePath);
        if (!file.exists()) {
            System.out.println("No initial data file found: " + storageFilePath);
            return;
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(file);


        try {
            Path path = Path.of(storageFilePath);
            if (!Files.exists(path)) {
                System.out.println("No initial data file found: " + storageFilePath);
                return;
            }

            Files.lines(path).forEach(line -> {
                String[] parts = line.split(",");
                String type = parts[0].trim().toLowerCase();

                if (root.has("trainers")) {
                    for (JsonNode t : root.get("trainers")) {
                        Trainer trainer = mapper.treeToValue(t, Trainer.class);
                        trainers.put(trainer.getId(), trainer);
                    }
                }

                // Load trainees
                if (root.has("trainees")) {
                    for (JsonNode t : root.get("trainees")) {
                        Trainee trainee = mapper.treeToValue(t, Trainee.class);
                        trainees.put(trainee.getId(), trainee);
                    }
                }

                // Load trainings
                if (root.has("trainings")) {
                    for (JsonNode t : root.get("trainings")) {
                        Training training = mapper.treeToValue(t, Training.class);
                        trainings.put(training.getTrainingId(), training);
                    }
                }


            });

            System.out.println("Storage initialized from file: " + storageFilePath);
        } catch (IOException e) {
            throw new RuntimeException("Error loading initial storage data", e);
        }
        
    }


}
