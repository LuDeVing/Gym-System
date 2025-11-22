package com.example.TrainingMicroservice.Stores;

import com.example.TrainingMicroservice.dto.TrainerSummaryDocument;
import com.example.TrainingMicroservice.repository.TrainerSummaryMongoRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.data.mongodb.core.FindAndModifyOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Repository
@Profile("mongo")
public class MongoTrainerSummaryStore implements TrainerSummaryStore {

    private final TrainerSummaryMongoRepository repo;
    private final MongoTemplate mongoTemplate;

    public MongoTrainerSummaryStore(TrainerSummaryMongoRepository repo, MongoTemplate mongoTemplate) {
        this.repo = repo;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void addDuration(String username, String firstName, String lastName, boolean isActive, int year, int month, int duration) {
        Query q = Query.query(Criteria.where("username").is(username));
        String field = String.format("years.%d.%d", year, month);
        Update u = new Update()
                .inc(field, duration)
                .setOnInsert("username", username)
                .setOnInsert("trainerFirstName", firstName)
                .setOnInsert("trainerLastName", lastName)
                .setOnInsert("isActive", isActive);
        FindAndModifyOptions opts = FindAndModifyOptions.options().upsert(true).returnNew(true);
        mongoTemplate.findAndModify(q, u, opts, TrainerSummaryDocument.class);
    }

    @Override
    public void deleteDuration(String username, int year, int month, int duration) {
        Optional<TrainerSummaryDocument> opt = repo.findByUsername(username);
        if (opt.isEmpty()) throw new IllegalArgumentException("Trainer not found");
        TrainerSummaryDocument doc = opt.get();
        Map<String, Map<String, Integer>> years = doc.getYears() != null ? doc.getYears() : new HashMap<>();
        String yk = String.valueOf(year), mk = String.valueOf(month);
        int current = years.containsKey(yk) && years.get(yk) != null ? years.get(yk).getOrDefault(mk, 0) : 0;
        int newVal = Math.max(0, current - duration);
        String field = String.format("years.%s.%s", yk, mk);
        Query q = Query.query(Criteria.where("username").is(username));
        Update u = new Update().set(field, newVal);
        mongoTemplate.updateFirst(q, u, TrainerSummaryDocument.class);
    }

    @Override
    public Map<String, Integer> getMonthlyHours(String username) {
        TrainerSummaryDocument doc = repo.findByUsername(username).orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
        Map<String, Integer> out = new HashMap<>();
        if (doc.getYears() != null) {
            doc.getYears().forEach((y, months) -> {
                if (months != null) months.forEach((m, h) -> out.put(y + "-" + m, h));
            });
        }
        return out;
    }
}
