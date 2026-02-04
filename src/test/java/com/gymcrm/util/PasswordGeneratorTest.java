package com.gymcrm.util;

import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    private final PasswordGenerator passwordGenerator = new PasswordGenerator();

    @Test
    @DisplayName("Should generate a password exactly 10 characters long")
    void generatePassword_CheckLength() {
        // ARRANGE - No mocks needed for this utility

        // ACT
        String password = passwordGenerator.generatePassword();

        // ASSERT
        assertNotNull(password);
        assertEquals(10, password.length(), "Password length must be 10 characters.");
    }

    @Test
    @DisplayName("Should generate different passwords on consecutive calls")
    void generatePassword_CheckRandomness() {
        // ARRANGE & ACT
        String pass1 = passwordGenerator.generatePassword();
        String pass2 = passwordGenerator.generatePassword();

        // ASSERT
        assertNotEquals(pass1, pass2, "Consecutive passwords should be random and different.");
    }
}