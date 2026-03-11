package com.pcs8.orientasi.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureStorageConfig {

    private static final Logger log = LoggerFactory.getLogger(AzureStorageConfig.class);

    @Value("${azure.storage.connection-string:}")
    private String connectionString;

    @Value("${azure.storage.container-name:pksi-documents}")
    private String containerName;

    @Bean
    @ConditionalOnProperty(name = "azure.storage.connection-string", matchIfMissing = false)
    public BlobServiceClient blobServiceClient() {
        if (connectionString == null || connectionString.isEmpty()) {
            log.warn("Azure Storage connection string is not configured. File upload will not work.");
            return null;
        }
        log.info("Initializing Azure Blob Storage client");
        return new BlobServiceClientBuilder()
                .connectionString(connectionString)
                .buildClient();
    }

    @Bean
    @ConditionalOnProperty(name = "azure.storage.connection-string", matchIfMissing = false)
    public BlobContainerClient blobContainerClient(@Autowired(required = false) BlobServiceClient blobServiceClient) {
        if (blobServiceClient == null) {
            log.warn("BlobServiceClient is null. File upload will not work.");
            return null;
        }
        BlobContainerClient containerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (Boolean.FALSE.equals(containerClient.exists())) {
            containerClient.create();
            log.info("Created Azure Blob container: {}", containerName);
        }
        return containerClient;
    }
}
