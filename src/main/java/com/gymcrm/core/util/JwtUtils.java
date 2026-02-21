package com.gymcrm.core.util;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import java.util.Date;

@Component
public class JwtUtils {
    // In a real app, move this to application.properties
    private static final String SECRET_KEY = "gym_crm_super_secret_key_change_me";
    private static final long EXPIRATION_TIME = 3600000; // 1 hour in ms

    private final Algorithm algorithm = Algorithm.HMAC256(SECRET_KEY);

    public String generateToken(String username) {
        return JWT.create()
                .withSubject(username)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .withIssuer("gym-crm")
                .sign(algorithm);
    }

    public String extractUsername(String token) {
        return getDecodedJWT(token).getSubject();
    }

    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }

    private boolean isTokenExpired(String token) {
        return getDecodedJWT(token).getExpiresAt().before(new Date());
    }

    private DecodedJWT getDecodedJWT(String token) {
        JWTVerifier verifier = JWT.require(algorithm)
                .withIssuer("gym-crm")
                .build();
        return verifier.verify(token);
    }
}
