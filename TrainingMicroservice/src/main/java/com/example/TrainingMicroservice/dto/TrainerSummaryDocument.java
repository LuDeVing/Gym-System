package com.example.TrainingMicroservice.dto;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

@Document(collection = "trainerSummaries")
@CompoundIndexes({
        @CompoundIndex(name = "name_idx", def = "{'trainerFirstName': 1, 'trainerLastName': 1}")
})
public class TrainerSummaryDocument {

    @Id
    private String id;

    @Indexed(unique = true)
    private String username;

    private String trainerFirstName;
    private String trainerLastName;
    private boolean isActive;

    private Map<String, Map<String, Integer>> years = new HashMap<>();

    protected TrainerSummaryDocument() {}

    public TrainerSummaryDocument(String username, String firstName, String lastName, boolean active) {
        this.username = username;
        this.trainerFirstName = firstName;
        this.trainerLastName = lastName;
        this.isActive = active;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getTrainerFirstName() {
        return trainerFirstName;
    }

    public void setTrainerFirstName(String trainerFirstName) {
        this.trainerFirstName = trainerFirstName;
    }

    public String getTrainerLastName() {
        return trainerLastName;
    }

    public void setTrainerLastName(String trainerLastName) {
        this.trainerLastName = trainerLastName;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public Map<String, Map<String, Integer>> getYears() {
        return years;
    }

    public void setYears(Map<String, Map<String, Integer>> years) {
        this.years = years;
    }
}
