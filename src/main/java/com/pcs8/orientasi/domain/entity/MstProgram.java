package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "mst_program", indexes = {
    @Index(name = "idx_program_rbsi_year", columnList = "rbsi_id, year_version"),
    @Index(name = "idx_program_sort_order", columnList = "rbsi_id, year_version, sort_order")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MstProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rbsi_id", nullable = false)
    private MstRbsi rbsi;

    @Column(name = "program_number", nullable = false, length = 20)
    private String programNumber;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "year_version", nullable = false)
    private Integer yearVersion;

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder;

    @Column(name = "status", length = 20)
    @Builder.Default
    private String status = "active";

    @Column(name = "start_date")
    private LocalDateTime startDate;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "created_by", length = 100)
    private String createdBy;

    @Column(name = "updated_by", length = 100)
    private String updatedBy;

    @OneToMany(mappedBy = "program", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MstInitiative> initiatives = new ArrayList<>();
}
