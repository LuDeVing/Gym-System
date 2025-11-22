package com.example.TrainingMicroservice.Stores;

import com.example.TrainingMicroservice.dto.TrainerSummary;
import com.example.TrainingMicroservice.repository.TrainerRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
@Profile("sql")
public class JpaTrainerSummaryStore implements TrainerSummaryStore {

    private final TrainerRepository repo;

    public JpaTrainerSummaryStore(TrainerRepository repo) {
        this.repo = repo;
    }

    @Override
    public void addDuration(String username, String firstName, String lastName, boolean isActive, int year, int month, int duration) {
        TrainerSummary summary = repo.findByUsername(username)
                .orElseGet(() -> new TrainerSummary(username, firstName, lastName, isActive));
        summary.addDuration(year, month, duration);
        repo.save(summary);
    }

    @Override
    public void deleteDuration(String username, int year, int month, int duration) {
        TrainerSummary summary = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        summary.deleteDuration(year, month, duration);
        repo.save(summary);
    }

    @Override
    public Map<String, Integer> getMonthlyHours(String username) {
        TrainerSummary summary = repo.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        Map<String, Integer> out = new HashMap<>();
        summary.getMonthlyHours().forEach((y, months) ->
            months.forEach((m, h) -> out.put(y + "-" + m, h))
        );
        return out;
    }
}
