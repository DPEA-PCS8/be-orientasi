package com.pcs8.orientasi.service.kafka;

import com.pcs8.orientasi.domain.dto.kafka.SubKategoriKafkaMessage;
import lombok.Getter;

/**
 * Raised inside a transaction when a sub kategori is created/updated/deleted.
 * Consumed after commit so Kafka is only written when the DB change is durable.
 * A null {@code message} means delete (tombstone for {@code kode}).
 */
@Getter
public class SubKategoriChangedEvent {

    private final String kode;
    private final SubKategoriKafkaMessage message;

    private SubKategoriChangedEvent(String kode, SubKategoriKafkaMessage message) {
        this.kode = kode;
        this.message = message;
    }

    public static SubKategoriChangedEvent upsert(SubKategoriKafkaMessage message) {
        return new SubKategoriChangedEvent(message.getKode(), message);
    }

    public static SubKategoriChangedEvent delete(String kode) {
        return new SubKategoriChangedEvent(kode, null);
    }
}
