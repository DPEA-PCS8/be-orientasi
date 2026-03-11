package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import static com.pcs8.orientasi.domain.constants.AplikasiFieldNames.*;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_komunikasi_sistem")
public class AplikasiSnapshotKomunikasiSistem extends BaseSnapshotChildEntity {

    @Column(name = NAMA_SISTEM, nullable = false, length = 255)
    private String namaSistem;

    @Column(name = TIPE_SISTEM, length = 50)
    private String tipeSistem;

    @Column(name = DESKRIPSI_KOMUNIKASI, columnDefinition = "NVARCHAR(MAX)")
    private String deskripsiKomunikasi;

    @Column(name = KETERANGAN, length = 500)
    private String keterangan;

    @Column(name = IS_PLANNED, nullable = false)
    @Builder.Default
    private Boolean isPlanned = false;
}
