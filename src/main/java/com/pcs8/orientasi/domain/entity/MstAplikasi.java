package com.pcs8.orientasi.domain.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.UuidGenerator;

import java.util.UUID;

@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "mst_aplikasi", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kode_aplikasi"})
})
public class MstAplikasi extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kode_aplikasi", nullable = false, length = 50)
    private String kodeAplikasi;

    @Column(name = "nama_aplikasi", nullable = false, length = 255)
    private String namaAplikasi;
}
