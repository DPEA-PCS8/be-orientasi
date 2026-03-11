package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_penghargaan")
public class AplikasiSnapshotPenghargaan extends BaseSnapshotChildEntity {

    @Column(name = "kategori_id")
    private UUID kategoriId;

    @Column(name = "kategori_kode", length = 50)
    private String kategoriKode;

    @Column(name = "kategori_nama", length = 255)
    private String kategoriNama;

    @Column(name = "tanggal", nullable = false)
    private LocalDate tanggal;

    @Column(name = "deskripsi", columnDefinition = "NVARCHAR(MAX)")
    private String deskripsi;
}
