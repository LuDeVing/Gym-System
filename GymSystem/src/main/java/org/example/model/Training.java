package org.example.model;

import java.time.LocalDate;
import java.util.Objects;

public class Training {
    private Long traineeId;
    private Long trainerId;
    private String trainingName;
    private TrainingType trainingType;
    private LocalDate trainingDate;
    private int trainingDuration;

    public Training() {}

    public Training(Long traineeId, Long trainerId, String trainingName, TrainingType trainingType, LocalDate trainingDate, int trainingDuration) {
        this.traineeId = traineeId;
        this.trainerId = trainerId;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
    }

    public Long getTrainingId(){
        return (long) Objects.hash(traineeId, trainerId, trainingDate, trainingType.getTrainingTypeName());
    }

    public Long getTraineeId() { return traineeId; }
    public void setTraineeId(Long traineeId) { this.traineeId = traineeId; }

    public Long getTrainerId() { return trainerId; }
    public void setTrainerId(Long trainerId) { this.trainerId = trainerId; }

    public String getTrainingName() { return trainingName; }
    public void setTrainingName(String trainingName) { this.trainingName = trainingName; }

    public TrainingType getTrainingType() { return trainingType; }
    public void setTrainingType(TrainingType trainingType) { this.trainingType = trainingType; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public int getTrainingDuration() { return trainingDuration; }
    public void setTrainingDuration(int trainingDuration) { this.trainingDuration = trainingDuration; }
}