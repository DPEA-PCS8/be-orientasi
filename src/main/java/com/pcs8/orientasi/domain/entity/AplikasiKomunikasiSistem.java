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
@Table(name = "trn_aplikasi_komunikasi_sistem")
public class AplikasiKomunikasiSistem extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_id", nullable = false)
    @ToString.Exclude
    private MstAplikasi aplikasi;

    @Column(name = "nama_sistem", nullable = false, length = 255)
    private String namaSistem;

    @Column(name = "tipe_sistem", length = 50)
    private String tipeSistem; // INTERNAL, EKSTERNAL

    @Column(name = "deskripsi_komunikasi", columnDefinition = "TEXT")
    private String deskripsiKomunikasi;

    @Column(name = "keterangan", length = 500)
    private String keterangan;

    @Column(name = "is_planned", nullable = false)
    @Builder.Default
    private Boolean isPlanned = false; // For planned future communications
}
