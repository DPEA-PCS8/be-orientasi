package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "rbsi_inisiatif", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"program_id", "tahun", "nomor_inisiatif"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiInisiatif {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private RbsiProgram program;

    @Column(name = "tahun", nullable = false)
    private Integer tahun;

    @Column(name = "nomor_inisiatif", nullable = false, length = 50)
    private String nomorInisiatif;

    @Column(name = "nama_inisiatif", nullable = false, length = 255)
    private String namaInisiatif;

    @Column(name = "pksi_id")
    private Long pksiId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
