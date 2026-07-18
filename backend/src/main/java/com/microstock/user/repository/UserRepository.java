package com.microstock.user.repository;

import com.microstock.user.domain.User;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmailIgnoreCase(String email);

    Optional<User> findByUsernameIgnoreCase(String username);

    /** Authentication by email OR username (AUTH-002). */
    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByUsernameIgnoreCase(String username);
}
