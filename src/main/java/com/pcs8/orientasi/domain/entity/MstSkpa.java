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
@Table(name = "mst_skpa", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kode_skpa"})
})
public class MstSkpa extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kode_skpa", nullable = false, length = 50)
    private String kodeSkpa;

    @Column(name = "nama_skpa", nullable = false, length = 255)
    private String namaSkpa;
}
