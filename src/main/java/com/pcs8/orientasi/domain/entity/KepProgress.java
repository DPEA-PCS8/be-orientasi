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
@Table(name = "trn_kep_progress", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"kep_id", "inisiatif_group_id", "tahun"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class KepProgress extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kep_id", nullable = false)
    private RbsiKep kep;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inisiatif_group_id", nullable = false)
    private InisiatifGroup inisiatifGroup;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "status", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private ProgressStatus status;

    public enum ProgressStatus {
        none,
        planned,
        realized
    }
}
