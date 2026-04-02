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
@Table(name = "trn_aplikasi_url")
public class AplikasiUrl extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_id", nullable = false)
    @ToString.Exclude
    private MstAplikasi aplikasi;

    @Column(name = "url", nullable = false, length = 500)
    private String url;

    @Column(name = "tipe_akses", length = 255)
    private String tipeAkses; // INTERNET, INTRANET, EXTRANET, DESKTOP_APP, MOBILE_APP, OTHER

    @Column(name = "keterangan", length = 255)
    private String keterangan;
}
