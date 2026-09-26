package com.bank.repository;

import com.bank.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository — handles all database operations for Users.
 *
 * By extending JpaRepository<User, Long>, Spring automatically gives us:
 *   - save(user)             → INSERT or UPDATE
 *   - findById(id)           → SELECT * FROM users WHERE id = ?
 *   - findAll()              → SELECT * FROM users
 *   - delete(user)           → DELETE FROM users WHERE id = ?
 *   - count()                → SELECT COUNT(*) FROM users
 *   - existsById(id)         → SELECT 1 FROM users WHERE id = ?
 *
 * We just add any custom queries we need as method signatures.
 * Spring generates the SQL automatically based on the method name!
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Spring translates this method name into:
     *   SELECT * FROM users WHERE email = ?
     *
     * Optional<User> means the result might be empty (user not found).
     * This forces us to handle the "not found" case explicitly.
     */
    Optional<User> findByEmail(String email);

    /**
     * SELECT 1 FROM users WHERE email = ?
     * Used during registration to check if email is already taken.
     */
    boolean existsByEmail(String email);
}
