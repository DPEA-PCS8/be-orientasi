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
 * ProgramGroup represents the logical entity of a program across different years.
 * Multiple RbsiProgram instances (across different years) can belong to the same group,
 * mirroring the same pattern used by InisiatifGroup for trn_rbsi_inisiatif.
 */
@Entity
@Table(name = "mst_program_group")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class ProgramGroup extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    private Rbsi rbsi;

    @Column(name = "nama_program", nullable = false, length = 255)
    private String namaProgram;

    @Column(name = "keterangan", length = 500)
    private String keterangan;

    @lombok.Builder.Default
    @Column(name = "is_deleted", nullable = false, columnDefinition = "BIT DEFAULT 0")
    private Boolean isDeleted = false;

    @OneToMany(mappedBy = "programGroup", cascade = CascadeType.ALL)
    @lombok.Builder.Default
    private List<RbsiProgram> programs = new ArrayList<>();
}
