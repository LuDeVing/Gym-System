package com.example.TrainingMicroservice.data;

import jakarta.persistence.*;

import java.util.HashMap;
import java.util.Map;

@Entity
public class TrainerSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String trainerFirstName;
    private String trainerLastName;
    private boolean isActive;

    @Convert(converter = MonthlyHoursConverter.class)
    private Map<Integer, Map<Integer, Integer>> monthlyHours = new HashMap<>();

    protected TrainerSummary() {}

    public TrainerSummary(String trainerUsername, String trainerFirstName, String trainerLastName, boolean isActive) {
        this.username = trainerUsername;
        this.trainerFirstName = trainerFirstName;
        this.trainerLastName = trainerLastName;
        this.isActive = isActive;
    }

    public String getUsername() { return username; }
    public String getTrainerFirstName() { return trainerFirstName; }
    public String getTrainerLastName() { return trainerLastName; }
    public boolean isActive() { return isActive; }
    public Map<Integer, Map<Integer, Integer>> getMonthlyHours() { return monthlyHours; }

    public void setActive(boolean active) { isActive = active; }

    public void addDuration(int year, int month, int duration) {
        monthlyHours.putIfAbsent(year, new HashMap<>());
        Map<Integer, Integer> months = monthlyHours.get(year);
        months.put(month, months.getOrDefault(month, 0) + duration);
    }

    public void deleteDuration(int year, int month, int duration) {
        monthlyHours.putIfAbsent(year, new HashMap<>());
        Map<Integer, Integer> months = monthlyHours.get(year);
        months.put(month, Math.max(0, months.getOrDefault(month, 0) - duration));
    }

    public int getTotalHours() {
        return monthlyHours.values().stream()
                .flatMap(monthMap -> monthMap.values().stream())
                .mapToInt(Integer::intValue)
                .sum();
    }

    public Long getId() {
        return id;
    }
}
