package com.pcs8.orientasi.config;

import com.pcs8.orientasi.constant.ConstantVariable;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.apache.kafka.clients.admin.NewTopic;

/**
 * Declares the compacted topic that carries mst_sub_kategori master data.
 * Log compaction keeps only the latest record per key (kode), so consumers can
 * rebuild full state from the topic alone.
 */
@Configuration
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true")
public class KafkaTopicConfig {

    @Bean
    public NewTopic subKategoriTopic() {
        return TopicBuilder.name(ConstantVariable.KAFKA_SUBKATEGORI_TOPIC)
                .partitions(1)
                .replicas(1)
                .config(TopicConfig.CLEANUP_POLICY_CONFIG, TopicConfig.CLEANUP_POLICY_COMPACT)
                .build();
    }
}
