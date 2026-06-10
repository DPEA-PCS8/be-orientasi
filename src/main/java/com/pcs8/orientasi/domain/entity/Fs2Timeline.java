package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Entity untuk timeline FS2 - mendukung multiple phases per stage
 */
@Entity
@Table(name = "trn_fs2_timeline")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Fs2Timeline extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fs2_id", nullable = false)
    private Fs2Document fs2Document;

    @Column(name = "phase", nullable = false)
    private Integer phase;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "stage", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TimelineStage stage;

    /**
     * Enum for FS2 timeline stages
     */
    public enum TimelineStage {
        PENGAJUAN, ASESMEN, PEMROGRAMAN, PENGUJIAN, DEPLOYMENT, GO_LIVE
    }
}
