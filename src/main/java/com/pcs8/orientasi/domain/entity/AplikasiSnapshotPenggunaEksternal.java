package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "his_aplikasi_pengguna_eksternal")
public class AplikasiSnapshotPenggunaEksternal extends BaseSnapshotChildEntity {

    @Column(name = "nama_pengguna", nullable = false, length = 255)
    private String namaPengguna;

    @Column(name = "keterangan", length = 500)
    private String keterangan;
}
