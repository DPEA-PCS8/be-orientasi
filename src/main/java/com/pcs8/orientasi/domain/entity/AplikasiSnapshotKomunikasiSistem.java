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
@Table(name = "his_aplikasi_komunikasi_sistem")
public class AplikasiSnapshotKomunikasiSistem extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    @ToString.Exclude
    private AplikasiSnapshot snapshot;

    @Column(name = "nama_sistem", nullable = false, length = 255)
    private String namaSistem;

    @Column(name = "tipe_sistem", length = 50)
    private String tipeSistem;

    @Column(name = "deskripsi_komunikasi", columnDefinition = "NVARCHAR(MAX)")
    private String deskripsiKomunikasi;

    @Column(name = "keterangan", length = 500)
    private String keterangan;

    @Column(name = "is_planned", nullable = false)
    @Builder.Default
    private Boolean isPlanned = false;
}
