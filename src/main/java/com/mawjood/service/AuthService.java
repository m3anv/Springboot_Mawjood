package com.mawjood.service;

import com.mawjood.model.Role;
import com.mawjood.model.User;
import com.mawjood.repository.OfficeRepository;
import com.mawjood.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Handles user authentication and account registration.
 *
 * Passwords are stored as plain text in this project (senior-project scope).
 * A production system would hash passwords with BCrypt or Argon2 before saving.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final OfficeRepository officeRepository;

    public AuthService(UserRepository userRepository, OfficeRepository officeRepository) {
        this.userRepository = userRepository;
        this.officeRepository = officeRepository;
    }

    // ── Authentication ────────────────────────────────────────────────────────

    /**
     * Attempts to log in with the given credentials.
     *
     * @return the matching User wrapped in Optional, or empty if no match found.
     */
    public Optional<User> login(String email, String password) {
        return userRepository.findByEmail(email)
                .filter(user -> user.getPassword().equals(password));
    }

    // ── Registration ──────────────────────────────────────────────────────────

    /**
     * Creates a new USER-role account and saves it to the database.
     *
     * @param name     display name
     * @param email    unique login email
     * @param password plain-text password
     * @return the saved User entity
     */
    public User registerUser(String name, String email, String password) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.USER);
        return userRepository.save(user);
    }

    /**
     * Creates a new OFFICE-role account linked to the given office.
     * Office accounts are created by the platform admin, not self-registered.
     *
     * @param officeId the ID of the office this user will manage
     */
    public User registerOfficeUser(String name, String email, String password, Long officeId) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(Role.OFFICE);
        user.setOffice(officeRepository.findById(officeId).orElseThrow());
        return userRepository.save(user);
    }

    // ── Validation ────────────────────────────────────────────────────────────

    /** Returns true if an account with this email already exists; used to prevent duplicates. */
    public boolean emailExists(String email) {
        return userRepository.findByEmail(email).isPresent();
    }
}
