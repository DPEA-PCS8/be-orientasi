package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_satker_internal")
public class AplikasiSnapshotSatkerInternal extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    @ToString.Exclude
    private AplikasiSnapshot snapshot;

    @Column(name = "nama_satker", nullable = false, length = 255)
    private String namaSatker;

    @Column(name = "keterangan", length = 500)
    private String keterangan;
}
