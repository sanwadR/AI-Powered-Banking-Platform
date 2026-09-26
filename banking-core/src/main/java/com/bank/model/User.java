package com.bank.model;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

/**
 * The User entity — maps to the "users" table in PostgreSQL.
 *
 * We implement UserDetails because Spring Security needs this interface
 * to understand who a user is and what they're allowed to do.
 *
 * Lombok annotations reduce boilerplate:
 *   @Getter     → generates all getXxx() methods
 *   @Setter     → generates all setXxx() methods
 *   @Builder    → enables User.builder().email("...").build() pattern
 *   @NoArgsConstructor → generates empty constructor (required by JPA)
 *   @AllArgsConstructor → generates constructor with all fields
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // Auto-increment
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;   // Always stored as bcrypt hash

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)  // Store "CUSTOMER" as text, not 0/1/2
    private Role role;

    @Builder.Default
    private boolean enabled = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ======================================================
    // JPA Lifecycle hooks — run automatically before save
    // ======================================================

    @PrePersist  // Called once, right before the first INSERT
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate  // Called before every UPDATE
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // ======================================================
    // UserDetails interface — required by Spring Security
    // ======================================================

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Tell Spring Security what role this user has
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;  // Spring Security uses email as the username
    }

    @Override
    public boolean isAccountNonExpired()  { return true; }

    @Override
    public boolean isAccountNonLocked()   { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }

    @Override
    public boolean isEnabled() { return enabled; }
}
