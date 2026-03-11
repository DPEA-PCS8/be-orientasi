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
@Table(name = "his_aplikasi_satker_internal")
public class AplikasiSnapshotSatkerInternal extends BaseSnapshotChildEntity {

    @Column(name = "nama_satker", nullable = false, length = 255)
    private String namaSatker;

    @Column(name = "keterangan", length = 500)
    private String keterangan;
}
