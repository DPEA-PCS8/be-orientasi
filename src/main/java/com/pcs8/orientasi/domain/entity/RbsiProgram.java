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
@Table(name = "trn_rbsi_program", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rbsi_id", "tahun", "nomor_program"})
})
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    private Rbsi rbsi;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "nomor_program", nullable = false, length = 50)
    private String nomorProgram;

    @Column(name = "nama_program", nullable = false, length = 255)
    private String namaProgram;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<RbsiInisiatif> inisiatifs = new ArrayList<>();
}
