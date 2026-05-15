package com.mawjood.repository;

import com.mawjood.model.Role;
import com.mawjood.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Database access for {@link User} records.
 *
 * Extends Spring Data's JpaRepository so all basic CRUD operations
 * (save, findById, delete, etc.) are available automatically.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /** Looks up a user by email address; used for login authentication. */
    Optional<User> findByEmail(String email);

    /** Returns all users with the given role; used by admin views and
     *  notification broadcasting (e.g. notifying all ADMINs). */
    List<User> findByRole(Role role);
}
