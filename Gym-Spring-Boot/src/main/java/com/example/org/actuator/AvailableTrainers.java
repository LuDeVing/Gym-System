package com.example.org.actuator;

import com.example.org.data.TrainerRepository;
import com.example.org.model.User;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AvailableTrainers {

    @Autowired
    private TrainerRepository trainerRepository;

    public AvailableTrainers(MeterRegistry meterRegistry, TrainerRepository trainerRepository){

        this.trainerRepository = trainerRepository;

        meterRegistry.gauge("gym.trainees.active.count", trainerRepository,
                repo -> repo.findAll().stream().filter(User::isActive).count());



    }



}
