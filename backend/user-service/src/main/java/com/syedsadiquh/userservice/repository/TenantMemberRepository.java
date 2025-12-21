package com.syedsadiquh.userservice.repository;

import com.syedsadiquh.userservice.entity.TenantMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantMemberRepository extends JpaRepository<TenantMember, UUID> {
}
