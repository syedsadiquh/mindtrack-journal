package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {

    List<Tag> findByTenantIdAndDeletedFalse(UUID tenantId);

    Optional<Tag> findByIdAndTenantIdAndDeletedFalse(UUID id, UUID tenantId);

    boolean existsByTenantIdAndNameIgnoreCaseAndDeletedFalse(UUID tenantId, String name);
}
