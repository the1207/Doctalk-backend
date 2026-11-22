package com.Doctalk.Doctalk_backend.security.repository;

import com.Doctalk.Doctalk_backend.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByUsername(String username);
}