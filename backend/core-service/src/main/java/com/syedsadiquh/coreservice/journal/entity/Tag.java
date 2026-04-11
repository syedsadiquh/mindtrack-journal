package com.syedsadiquh.coreservice.journal.entity;

import com.syedsadiquh.coreservice.shared.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.UUID;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Entity
@Table(name = "tags", indexes = {
        @Index(name = "idx_tag_tenant_id", columnList = "tenant_id")
})
/* NOTE: -
  Unique constraint is to be maintained by the application layer to allow the same tag names across different tenants,
  This is to prevent error on recreating new tags with same name after soft deletion of old tags.
  The application layer should check for existing active tags with the same name before creating a new tag.
 */
public class Tag extends BaseEntity {

    @Column(nullable = false)
    private UUID tenantId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 20)
    private String color;
}
