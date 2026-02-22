package com.gymcrm.service;

import com.gymcrm.service.interfaces.LoginAttemptService;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {

    // Key: username, Value: Attempts
    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    // Key: username, Value: Unblock Time
    private final Map<String, LocalDateTime> blockCache = new ConcurrentHashMap<>();
    @Setter private Clock clock = Clock.systemDefaultZone();

    public void loginSucceeded(String username) {
        attemptsCache.remove(username);
        blockCache.remove(username);
    }

    public void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0) + 1;
        attemptsCache.put(username, attempts);

        int MAX_ATTEMPTS = 3;
        if (attempts >= MAX_ATTEMPTS) {
            int BLOCK_DURATION_MINUTES = 5;
            blockCache.put(username, LocalDateTime.now(clock).plusMinutes(BLOCK_DURATION_MINUTES));
        }
    }

    public boolean isBlocked(String username) {
        if (blockCache.containsKey(username)) {
            if (LocalDateTime.now(clock).isAfter(blockCache.get(username))) {
                blockCache.remove(username);
                attemptsCache.remove(username);
                return false;
            }
            return true;
        }
        return false;
    }
}
