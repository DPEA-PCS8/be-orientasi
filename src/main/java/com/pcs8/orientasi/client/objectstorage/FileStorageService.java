package com.pcs8.orientasi.client.objectstorage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String uploadFile(MultipartFile file, String ownerType, String ownerId);
    String getFileUrl(String storageId);
    byte[] downloadFile(String storageId);
    void deleteFile(String storageId);
    boolean fileExists(String storageId);
}
