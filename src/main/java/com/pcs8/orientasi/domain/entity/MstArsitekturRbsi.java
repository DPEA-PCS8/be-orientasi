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
@Table(name = "mst_arsitektur_rbsi")
public class MstArsitekturRbsi extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    @ToString.Exclude
    private Rbsi rbsi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_kategori_id", nullable = false)
    @ToString.Exclude
    private MstSubKategori subKategori;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_baseline_id")
    @ToString.Exclude
    private MstAplikasi aplikasiBaseline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aplikasi_target_id")
    @ToString.Exclude
    private MstAplikasi aplikasiTarget;

    @Column(name = "action", length = 50)
    private String action;

    @Column(name = "year_statuses", length = 500)
    private String yearStatuses;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inisiatif_id")
    @ToString.Exclude
    private RbsiInisiatif inisiatif;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "skpa_id")
    @ToString.Exclude
    private MstSkpa skpa;
}
