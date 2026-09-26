package com.bank.config;

import com.bank.repository.UserRepository;
import com.bank.security.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * SecurityConfig — the master security configuration for the application.
 *
 * This class answers:
 *   - Which endpoints are public? Which require login?
 *   - How do we verify passwords?
 *   - How do we load users from the database?
 *   - Where does our JWT filter plug in?
 *
 * @Configuration  = this class provides Spring beans
 * @EnableWebSecurity = activate Spring Security
 * @EnableMethodSecurity = allows @PreAuthorize on controller methods
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final UserRepository userRepository;

    /**
     * The main security filter chain — defines the rules.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF (Cross-Site Request Forgery) protection.
            // CSRF is for browser-based sessions. We use stateless JWT tokens, so we don't need it.
            .csrf(AbstractHttpConfigurer::disable)

            // Define which URLs require authentication
            .authorizeHttpRequests(auth -> auth
                // These endpoints are PUBLIC — no login required
                .requestMatchers(
                    "/api/v1/auth/**",       // Register and login
                    "/swagger-ui/**",        // API documentation
                    "/swagger-ui.html",
                    "/api-docs/**",
                    "/actuator/health"       // Health check
                ).permitAll()

                // Admin-only endpoints
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")

                // Everything else requires a valid JWT token
                .anyRequest().authenticated()
            )

            // Use STATELESS sessions — no server-side session stored.
            // Every request must include the JWT token. The server doesn't "remember" you.
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // Tell Spring Security how to verify users
            .authenticationProvider(authenticationProvider())

            // Add our JWT filter BEFORE Spring's default username/password filter
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * AuthenticationProvider — Spring Security uses this to verify credentials.
     * DaoAuthenticationProvider:
     *   1. Loads user from database (via UserDetailsService)
     *   2. Compares the provided password to the stored bcrypt hash
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService());
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * UserDetailsService — how Spring Security loads a user by their username (email).
     */
    @Bean
    public UserDetailsService userDetailsService() {
        return username -> userRepository.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                    "User not found with email: " + username
                ));
    }

    /**
     * PasswordEncoder — bcrypt is the industry standard for hashing passwords.
     * It's deliberately slow and adds a random "salt" so even identical passwords
     * produce different hashes.
     *
     * NEVER store plain text passwords. Always use this encoder.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager — used in AuthService to authenticate login requests.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config)
            throws Exception {
        return config.getAuthenticationManager();
    }
}
