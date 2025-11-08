package com.example.org.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {

    private final int MAX_ATTEMPT = 3;
    private final long LOCK_TIME = 5 * 60 * 1000;

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attempts.remove(username);
    }

    public void loginFailed(String username) {
        LoginAttempt attempt = attempts.getOrDefault(username, new LoginAttempt(0, null));
        attempt.increment();
        attempts.put(username, attempt);
    }

    public boolean isBlocked(String username) {
        LoginAttempt attempt = attempts.get(username);
        if (attempt == null) return false;

        if (attempt.getCount() >= MAX_ATTEMPT) {
            if (attempt.getLastFailedTime() != null &&
                attempt.getLastFailedTime().plusMinutes(5).isAfter(LocalDateTime.now())) {
                return true;
            } else {
                attempts.remove(username);
                return false;
            }
        }
        return false;
    }

    private static class LoginAttempt {
        private int count;
        private LocalDateTime lastFailedTime;

        public LoginAttempt(int count, LocalDateTime lastFailedTime) {
            this.count = count;
            this.lastFailedTime = lastFailedTime;
        }

        public void increment() {
            count++;
            lastFailedTime = LocalDateTime.now();
        }

        public int getCount() { return count; }
        public LocalDateTime getLastFailedTime() { return lastFailedTime; }
    }
}
