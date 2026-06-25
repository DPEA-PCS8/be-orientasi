package com.pcs8.orientasi.service.kafka;

import com.pcs8.orientasi.constant.ConstantVariable;
import com.pcs8.orientasi.domain.dto.kafka.SubKategoriKafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Ships sub kategori changes to the compacted Kafka topic AFTER the originating
 * transaction commits, so a rolled-back change is never published. Send failures
 * are logged but not rethrown; the compacted topic plus the resync endpoint allow
 * consumers to recover any gap.
 */
@Component
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class SubKategoriKafkaProducer {

    private static final Logger log = LoggerFactory.getLogger(SubKategoriKafkaProducer.class);

    private static final String TOPIC = ConstantVariable.KAFKA_SUBKATEGORI_TOPIC;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public SubKategoriKafkaProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSubKategoriChanged(SubKategoriChangedEvent event) {
        if (event.getMessage() == null) {
            publishTombstone(event.getKode());
        } else {
            publish(event.getMessage());
        }
    }

    /** Publish latest state for a sub kategori, keyed by kode. */
    public void publish(SubKategoriKafkaMessage message) {
        kafkaTemplate.send(TOPIC, message.getKode(), message)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish sub kategori {} to Kafka: {}", message.getKode(), ex.getMessage(), ex);
                    } else {
                        log.info("Published sub kategori to Kafka: {} - {} (partition {}, offset {})",
                                message.getKode(), message.getNama(),
                                result.getRecordMetadata().partition(), result.getRecordMetadata().offset());
                    }
                });
    }

    /** Publish a tombstone (null value) so compaction removes the key. */
    public void publishTombstone(String kode) {
        kafkaTemplate.send(TOPIC, kode, null)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.error("Failed to publish tombstone for sub kategori {} to Kafka: {}", kode, ex.getMessage(), ex);
                    } else {
                        log.info("Published sub kategori tombstone to Kafka: {}", kode);
                    }
                });
    }
}
