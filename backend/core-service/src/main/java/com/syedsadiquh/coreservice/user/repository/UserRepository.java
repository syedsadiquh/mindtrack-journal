package com.syedsadiquh.coreservice.user.repository;

import com.syedsadiquh.coreservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.swing.text.html.Option;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    User findByEmail(String email);
}

