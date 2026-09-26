package com.bank.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * JwtService — handles everything related to JWT tokens.
 *
 * Responsibilities:
 *   1. Generate a token when a user logs in
 *   2. Extract information (like email) from an existing token
 *   3. Validate that a token is genuine and not expired
 *
 * @Component marks this class as a Spring bean — Spring manages its lifecycle.
 * @Slf4j (Lombok) gives us a 'log' variable for logging.
 */
@Slf4j
@Component
public class JwtService {

    // Injected from application.yml: jwt.secret
    @Value("${jwt.secret}")
    private String secretKey;

    // Injected from application.yml: jwt.expiration-ms
    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    // ============================================================
    // TOKEN GENERATION
    // ============================================================

    /**
     * Generate a JWT token for a user after successful login.
     * The token contains the user's email and role.
     */
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> extraClaims = new HashMap<>();
        // Add the user's role into the token payload
        extraClaims.put("role", userDetails.getAuthorities().iterator().next().getAuthority());
        return generateToken(extraClaims, userDetails);
    }

    private String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(userDetails.getUsername())       // Email as the subject
                .issuedAt(new Date())                     // When the token was created
                .expiration(new Date(System.currentTimeMillis() + expirationMs))  // When it expires
                .signWith(getSigningKey())                // Sign it with our secret key
                .compact();                               // Build the final string
    }

    // ============================================================
    // TOKEN VALIDATION
    // ============================================================

    /**
     * Check if a token is valid for a given user.
     * Valid means: email matches AND token isn't expired.
     */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    // ============================================================
    // CLAIM EXTRACTION (reading data from the token)
    // ============================================================

    /**
     * Extract the email (username) from a token.
     * This is called on every request to identify who is making the request.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic method to extract any claim from a token.
     * A "claim" is a piece of data stored inside the token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())   // Use our secret key to verify the signature
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * Convert our hex secret string into a cryptographic key.
     * This key is used to sign and verify tokens.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = hexStringToByteArray(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
                    + Character.digit(s.charAt(i + 1), 16));
        }
        return data;
    }
}
