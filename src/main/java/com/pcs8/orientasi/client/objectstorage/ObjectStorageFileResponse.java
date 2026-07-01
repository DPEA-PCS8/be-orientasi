package com.pcs8.orientasi.client.objectstorage;

import java.time.Instant;
import java.util.UUID;

public record ObjectStorageFileResponse(
    UUID id,
    String ownerType,
    String ownerId,
    String objectKey,
    String originalFilename,
    String mimeType,
    long sizeBytes,
    String accessLevel,
    String url,
    Instant createdAt
) {}
