package com.example.org.authorization;

import com.example.org.facade.GymFacade;
import com.example.org.model.Trainee;
import com.example.org.model.Trainer;
import com.example.org.service.LoginAttemptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class mainUserDetailService implements UserDetailsService {

    @Autowired
    private GymFacade gymFacade;

    @Autowired
    private LoginAttemptService loginAttemptService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        if (loginAttemptService.isBlocked(username)) {
            throw new LockedException("User blocked due to 3 failed login attempts. Try again in 5 minutes.");
        }

        Optional<Trainee> trainee = gymFacade.selectByTraineeName(username);

        if (trainee.isPresent()) {
            return org.springframework.security.core.userdetails.User.builder()
                    .username(trainee.get().getUsername())
                    .password(trainee.get().getPassword())
                    .roles("TRAINEE")
                    .build();
        }

        Optional<Trainer> trainer = gymFacade.selectTrainerByUserName(username);

        if (trainer.isPresent()){
            return org.springframework.security.core.userdetails.User.builder()
                    .username(trainer.get().getUsername())
                    .password(trainer.get().getPassword())
                    .roles("TRAINER")
                    .build();
        }

        throw new UsernameNotFoundException("User not found");

    }
}
