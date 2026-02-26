package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * InisiatifGroup represents the logical entity of an inisiatif across different years.
 * Multiple RbsiInisiatif instances (across different years) can belong to the same group,
 * allowing KepProgress to be shared across all instances of the same logical inisiatif.
 */
@Entity
@Table(name = "mst_inisiatif_group")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class InisiatifGroup extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    private Rbsi rbsi;

    @Column(name = "nama_inisiatif", nullable = false, length = 255)
    private String namaInisiatif;

    @Column(name = "keterangan", length = 500)
    private String keterangan;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL)
    @lombok.Builder.Default
    private List<RbsiInisiatif> inisiatifs = new ArrayList<>();

    @OneToMany(mappedBy = "inisiatifGroup", cascade = CascadeType.ALL)
    @lombok.Builder.Default
    private List<KepProgress> kepProgresses = new ArrayList<>();
}
