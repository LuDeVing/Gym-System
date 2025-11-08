package com.example.org.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "trainings")
public class Training {

    @Id
    @Schema(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "trainee_id", nullable = false)
    @Schema(name = "trainee")
    private Trainee trainee;

    @ManyToOne
    @JoinColumn(name = "trainer_id", nullable = false)
    @Schema(name = "trainer")
    private Trainer trainer;

    @Column(name = "training_date", nullable = false)
    @Schema(name = "trainingDate")
    private LocalDate trainingDate;

    @Column(name = "trainingname", nullable = false)
    @Schema(name = "trainingName")
    private String trainingName;

    @ManyToOne
    @JoinColumn(name = "training_type", nullable = false)
    @Schema(name = "trainingType")
    private TrainingType trainingType;

    @Column(name = "trainingduration", nullable = false)
    @Schema(name = "trainingDuration")
    private int trainingDuration;

    public Training() {}

    public Training(Trainee trainee, Trainer trainer, String trainingName,
                    TrainingType trainingType, LocalDate trainingDate, int trainingDuration) {
        this.trainee = trainee;
        this.trainer = trainer;
        this.trainingName = trainingName;
        this.trainingType = trainingType;
        this.trainingDate = trainingDate;
        this.trainingDuration = trainingDuration;
        this.id = generateNumericHash(trainee.getUserId(), trainer.getUserId(), trainingDate);
    }

    public Long getId() { return id; }
    public Trainee getTrainee() { return trainee; }
    public Trainer getTrainer() { return trainer; }
    public LocalDate getTrainingDate() { return trainingDate; }
    public String getTrainingName() { return trainingName; }
    public TrainingType getTrainingType() { return trainingType; }
    public int getTrainingDuration() { return trainingDuration; }

    public static Long generateNumericHash(Long traineeId, Long trainerId, LocalDate date) {
        long hash = 7;
        hash = 31 * hash + traineeId;
        hash = 31 * hash + trainerId;
        hash = 31 * hash + date.toString().hashCode();
        return hash;
    }
}
