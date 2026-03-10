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
@Table(name = "his_aplikasi_url")
public class AplikasiSnapshotUrl extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "snapshot_id", nullable = false)
    @ToString.Exclude
    private AplikasiSnapshot snapshot;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "tipe_akses", length = 20)
    private String tipeAkses;

    @Column(name = "keterangan", length = 255)
    private String keterangan;
}
