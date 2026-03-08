package com.syedsadiquh.coreservice.user.repository;

import com.syedsadiquh.coreservice.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    User findByUsername(String username);
}

