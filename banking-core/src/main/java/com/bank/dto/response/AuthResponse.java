package com.bank.dto.response;

import lombok.Builder;
import lombok.Data;

/**
 * AuthResponse — what we send back after successful login or registration.
 *
 * The client stores this token and includes it in every future request
 * as: Authorization: Bearer <token>
 */
@Data
@Builder
public class AuthResponse {
    private String token;
    private String tokenType;    // Always "Bearer"
    private long expiresIn;      // Milliseconds until token expires
    private String email;
    private String fullName;
    private String role;
}
