package com.gymcrm.service;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptService {
    private final int MAX_ATTEMPTS = 3;
    private final int BLOCK_DURATION_MINUTES = 5;

    // Key: username, Value: Attempts
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    // Key: username, Value: Unblock Time
    private final Map<String, LocalDateTime> blockCache = new ConcurrentHashMap<>();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        blockCache.remove(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0) + 1;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            blockCache.put(username, LocalDateTime.now().plusMinutes(BLOCK_DURATION_MINUTES));
        }
    }

    public boolean isBlocked(String username) {
        if (blockCache.containsKey(username)) {
            if (LocalDateTime.now().isAfter(blockCache.get(username))) {
                blockCache.remove(username);
                attemptsCache.remove(username);
                return false;
            }
            return true;
        }
        return false;
    }
}
