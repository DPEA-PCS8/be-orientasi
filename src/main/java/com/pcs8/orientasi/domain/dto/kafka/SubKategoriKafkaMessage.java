package com.pcs8.orientasi.domain.dto.kafka;

import com.pcs8.orientasi.domain.entity.MstSubKategori;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Payload published to the compacted Kafka topic for mst_sub_kategori master data.
 * Keyed by {@code kode}; a tombstone (null value) on the same key marks a delete.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubKategoriKafkaMessage {

    private UUID id;
    private String kode;
    private String nama;
    private String categoryCode;
    private String categoryName;

    public static SubKategoriKafkaMessage from(MstSubKategori entity) {
        return SubKategoriKafkaMessage.builder()
                .id(entity.getId())
                .kode(entity.getKode())
                .nama(entity.getNama())
                .categoryCode(entity.getCategoryCode())
                .categoryName(entity.getCategoryName())
                .build();
    }
}
