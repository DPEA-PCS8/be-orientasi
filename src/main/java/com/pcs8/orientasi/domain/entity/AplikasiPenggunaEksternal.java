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
@Table(name = "trn_aplikasi_pengguna_eksternal")
public class AplikasiPenggunaEksternal extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_id", nullable = false)
    @ToString.Exclude
    private MstAplikasi aplikasi;

    @Column(name = "nama_pengguna", nullable = false, length = 255)
    private String namaPengguna;

    @Column(name = "keterangan", length = 500)
    private String keterangan;
}
