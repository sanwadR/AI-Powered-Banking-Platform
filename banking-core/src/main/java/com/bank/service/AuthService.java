package com.bank.service;

import com.bank.dto.request.LoginRequest;
import com.bank.dto.request.RegisterRequest;
import com.bank.dto.response.AuthResponse;
import com.bank.exception.EmailAlreadyExistsException;
import com.bank.model.Role;
import com.bank.model.User;
import com.bank.repository.UserRepository;
import com.bank.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * AuthService — handles user registration and login.
 *
 * @Service = Spring bean that lives in the service layer
 * @Transactional = wraps methods in a database transaction automatically
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    // ============================================================
    // REGISTRATION
    // ============================================================

    /**
     * Register a new customer.
     *
     * Steps:
     * 1. Check email isn't already taken
     * 2. Hash the password
     * 3. Save user to database
     * 4. Generate and return a JWT token
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Step 1: Check for duplicate email
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(
                "An account with email '" + request.getEmail() + "' already exists"
            );
        }

        // Step 2: Build the User entity
        User user = User.builder()
                .fullName(request.getFullName())
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))  // Hash the password!
                .role(Role.CUSTOMER)   // All self-registered users are CUSTOMER by default
                .enabled(true)
                .build();

        // Step 3: Save to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with ID: {}", savedUser.getId());

        // Step 4: Generate JWT token
        String token = jwtService.generateToken(savedUser);

        return buildAuthResponse(token, savedUser);
    }

    // ============================================================
    // LOGIN
    // ============================================================

    /**
     * Authenticate a user with email and password.
     *
     * Steps:
     * 1. Ask Spring Security to verify the credentials
     *    (it will load the user from DB and compare bcrypt hashes)
     * 2. If valid, generate and return a JWT token
     * 3. If invalid, Spring Security throws BadCredentialsException automatically
     */
    public AuthResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // This single line does everything:
        // - Loads user by email from database
        // - Hashes the provided password and compares with stored hash
        // - Throws BadCredentialsException if wrong
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail().toLowerCase().trim(),
                        request.getPassword()
                )
        );

        // If we reach here, credentials are valid
        User user = userRepository.findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new RuntimeException("User not found after authentication"));

        log.info("User logged in successfully: {}", user.getEmail());

        String token = jwtService.generateToken(user);
        return buildAuthResponse(token, user);
    }

    // ============================================================
    // HELPERS
    // ============================================================

    private AuthResponse buildAuthResponse(String token, User user) {
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .expiresIn(86400000L)  // 24 hours
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .build();
    }
}
