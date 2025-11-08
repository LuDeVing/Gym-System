package com.example.org.model;


import jakarta.persistence.*;
import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "training_types")
@Immutable
public class TrainingType {

    @Id
    @Column(name = "trainingtypename", nullable = false, updatable = false)
    private String trainingTypeName;

    public TrainingType() {}

    public TrainingType(String trainingTypeName) {
        this.trainingTypeName = trainingTypeName;
    }

    public String getTrainingTypeName() { return trainingTypeName; }
    public void setTrainingTypeName(String trainingTypeName) { this.trainingTypeName = trainingTypeName; }

}
