package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

/**
 * Entity untuk dokumen T.01 (PKSI) - MVP Version
 */
@Entity
@Table(name = "trn_pksi_document")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PksiDocument extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private MstUser user;

    // Header
    @Column(name = "nama_pksi", nullable = false, length = 255)
    private String namaPksi;

    // Section 1 - Pendahuluan
    @Column(name = "deskripsi_pksi", columnDefinition = "TEXT", nullable = false)
    private String deskripsiPksi;

    @Column(name = "tujuan_pengajuan", columnDefinition = "TEXT", nullable = false)
    private String tujuanPengajuan;

    @Column(name = "kapan_diselesaikan", length = 255, nullable = false)
    private String kapanDiselesaikan;

    @Column(name = "pic_satker", length = 255, nullable = false)
    private String picSatker;

    // Section 2 - Tujuan
    @Column(name = "tujuan_pksi", columnDefinition = "TEXT", nullable = false)
    private String tujuanPksi;

    // Section 3 - Cakupan
    @Column(name = "ruang_lingkup", columnDefinition = "TEXT", nullable = false)
    private String ruangLingkup;

    // Section 5 - Gambaran Umum Aplikasi
    @Column(name = "pengelola_aplikasi", length = 255, nullable = false)
    private String pengelolaAplikasi;

    @Column(name = "pengguna_aplikasi", columnDefinition = "TEXT", nullable = false)
    private String penggunaAplikasi;

    @Column(name = "program_inisiatif_rbsi", length = 100, nullable = false)
    private String programInisiatifRbsi;

    @Column(name = "fungsi_aplikasi", columnDefinition = "TEXT", nullable = false)
    private String fungsiAplikasi;

    // Status
    @Column(name = "status", length = 50)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    public enum DocumentStatus {
        DRAFT,
        SUBMITTED,
        APPROVED,
        REJECTED,
        REVISION
    }
}
