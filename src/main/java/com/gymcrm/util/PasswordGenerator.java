package com.gymcrm.util;

import org.springframework.stereotype.Component;
import java.security.SecureRandom;

@Component
public class PasswordGenerator {

    /**
     * Generates a random 10-character password, according to requirement 7, bullet point 3:
     * "Password should be generated as a random 10 chars length string."
     */

    public String generatePassword(){
        final String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            // From 0 to 9, so length is 10 characters
            // It randomly selects one character from "chars" at each iteration, and appends them
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}