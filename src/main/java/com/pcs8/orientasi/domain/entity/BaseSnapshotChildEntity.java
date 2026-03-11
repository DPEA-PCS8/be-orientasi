package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

import static com.pcs8.orientasi.domain.constants.AplikasiFieldNames.ID;
import static com.pcs8.orientasi.domain.constants.AplikasiFieldNames.SNAPSHOT_ID;

/**
 * Base class for all entities that are children of AplikasiSnapshot.
 * Reduces code duplication by providing common fields and mappings.
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@MappedSuperclass
public abstract class BaseSnapshotChildEntity extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = ID, updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = SNAPSHOT_ID, nullable = false)
    @ToString.Exclude
    private AplikasiSnapshot snapshot;
}
