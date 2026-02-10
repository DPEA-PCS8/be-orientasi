package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "rbsi_program", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"rbsi_id", "tahun", "nomor_program"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RbsiProgram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

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
