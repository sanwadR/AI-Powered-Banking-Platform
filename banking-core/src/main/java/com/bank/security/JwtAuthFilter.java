package com.bank.security;

import com.bank.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JwtAuthFilter — intercepts EVERY HTTP request and checks for a JWT token.
 *
 * How it works:
 * 1. Look for "Authorization: Bearer <token>" in the request header
 * 2. Extract the token
 * 3. Validate it
 * 4. If valid, tell Spring Security "this request is from user X"
 * 5. Let the request continue to the controller
 *
 * OncePerRequestFilter ensures this filter runs exactly once per request.
 * @RequiredArgsConstructor (Lombok) generates a constructor for all final fields.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Get the Authorization header
        final String authHeader = request.getHeader("Authorization");

        // 2. If no header or doesn't start with "Bearer ", skip JWT auth
        //    The request will fail at the controller level if the endpoint requires auth
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. Extract the token (remove "Bearer " prefix)
        final String jwt = authHeader.substring(7);

        try {
            // 4. Extract email from the token
            final String userEmail = jwtService.extractUsername(jwt);

            // 5. Only proceed if we got an email AND no auth is set yet
            if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 6. Load user from database
                UserDetails userDetails = userRepository.findByEmail(userEmail)
                        .orElseThrow(() -> new RuntimeException("User not found"));

                // 7. Validate the token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // 8. Create an authentication object and put it in the security context
                    //    This is how Spring Security knows "this request is authenticated"
                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,   // No credentials needed (token already verified)
                                    userDetails.getAuthorities()
                            );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Token is invalid/expired — log it and continue without authentication
            // The request will fail at the controller if auth is required
            log.warn("JWT validation failed: {}", e.getMessage());
        }

        // 9. Continue the request to the next filter or controller
        filterChain.doFilter(request, response);
    }
}
