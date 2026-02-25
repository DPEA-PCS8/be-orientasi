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
@Table(name = "trn_rbsi_kep", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rbsi_id", "nomor_kep"}),
        @UniqueConstraint(columnNames = {"rbsi_id", "tahun_pelaporan"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiKep extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    private Rbsi rbsi;

    @Column(name = "nomor_kep", nullable = false, length = 50)
    private String nomorKep;

    @Column(name = "tahun_pelaporan", nullable = false)
    private Integer tahunPelaporan;

    @OneToMany(mappedBy = "kep", cascade = CascadeType.ALL, orphanRemoval = true)
    @Default
    private List<KepProgress> progressList = new ArrayList<>();
}
