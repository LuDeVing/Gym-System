package com.example.TrainingMicroservice.Stores;

import java.util.Map;

public interface TrainerSummaryStore {
    void addDuration(String username, String firstName, String lastName, boolean isActive,
                     int year, int month, int duration);

    void deleteDuration(String username, int year, int month, int duration);

    Map<String, Integer> getMonthlyHours(String username);
}
