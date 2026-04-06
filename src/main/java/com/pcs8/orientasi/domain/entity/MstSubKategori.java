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
@Table(name = "mst_sub_kategori", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"kode"})
})
public class MstSubKategori extends BaseEntity {

    @Id
    @UuidGenerator
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "kode", nullable = false, length = 20)
    private String kode;

    @Column(name = "nama", nullable = false, length = 255)
    private String nama;

    @Column(name = "category_code", nullable = false, length = 10)
    private String categoryCode;

    @Column(name = "category_name", nullable = false, length = 100)
    private String categoryName;
}
