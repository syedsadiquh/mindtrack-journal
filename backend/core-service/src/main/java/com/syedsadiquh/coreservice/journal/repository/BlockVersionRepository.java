package com.syedsadiquh.coreservice.journal.repository;

import com.syedsadiquh.coreservice.journal.entity.BlockVersion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface BlockVersionRepository extends JpaRepository<BlockVersion, UUID> {

    List<BlockVersion> findByBlockIdOrderByVersionNumberDesc(UUID blockId);

    int countByBlockId(UUID blockId);
}
