package com.bank.controller;

import com.bank.dto.request.LoginRequest;
import com.bank.dto.request.RegisterRequest;
import com.bank.dto.response.AuthResponse;
import com.bank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AuthController — handles HTTP requests for authentication.
 *
 * @RestController = @Controller + @ResponseBody
 *   Tells Spring: "This class handles HTTP requests and returns JSON"
 *
 * @RequestMapping = all routes in this class start with /api/v1/auth
 *
 * ResponseEntity<T> lets us control both the response body AND the HTTP status code.
 *   HTTP 200 OK          = success (read/update)
 *   HTTP 201 CREATED     = success (new resource created)
 *   HTTP 400 BAD REQUEST = validation error
 *   HTTP 401 UNAUTHORIZED = wrong credentials
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    /**
     * POST /api/v1/auth/register
     *
     * @Valid triggers validation of the RegisterRequest object.
     * If any @NotBlank, @Email, @Size rules fail, Spring automatically
     * returns a 400 Bad Request with error details.
     */
    @PostMapping("/register")
    @Operation(summary = "Register a new customer account")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * POST /api/v1/auth/login
     */
    @PostMapping("/login")
    @Operation(summary = "Login with email and password, receive JWT token")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
