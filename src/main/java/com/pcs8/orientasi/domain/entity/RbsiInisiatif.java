package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Entity
@Table(name = "mst_rbsi_inisiatif", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"program_id", "tahun", "nomor_inisiatif"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiInisiatif extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private RbsiProgram program;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "nomor_inisiatif", nullable = false, length = 50)
    private String nomorInisiatif;

    @Column(name = "nama_inisiatif", nullable = false, length = 255)
    private String namaInisiatif;
}
