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
 * Entity untuk timeline PKSI - mendukung multiple phases per stage
 */
@Entity
@Table(name = "trn_pksi_timeline")
@Data
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class PksiTimeline extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pksi_id", nullable = false)
    private PksiDocument pksiDocument;

    @Column(name = "phase", nullable = false)
    private Integer phase;

    @Column(name = "target_date", nullable = false)
    private LocalDate targetDate;

    @Column(name = "stage", nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    private TimelineStage stage;

    /**
     * Enum for timeline stages
     */
    public enum TimelineStage {
        USREQ, SIT, UAT, GO_LIVE, PENGADAAN, DESAIN, CODING, UNIT_TEST, DEPLOYMENT
    }
}
