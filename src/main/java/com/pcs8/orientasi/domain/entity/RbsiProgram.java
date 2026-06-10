package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;
import org.hibernate.annotations.UuidGenerator;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trn_rbsi_program")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiProgram extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    // TODO: review — program_group_id is nullable for now to support migration of existing data.
    //       Once all existing programs are backfilled with a group, consider making it NOT NULL.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_group_id", nullable = true)
    private ProgramGroup programGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kep_id", nullable = false)
    private RbsiKep kep;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "nomor_program", nullable = false, length = 50)
    private String nomorProgram;

    @Column(name = "nama_program", nullable = false, length = 255)
    private String namaProgram;

    @Default
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<RbsiInisiatif> inisiatifs = new ArrayList<>();
}
