package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.exception.MinioOperationException;
import com.pcs8.orientasi.service.MinioService;
import io.minio.*;
import io.minio.http.Method;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
public class MinioServiceImpl implements MinioService {

    private static final Logger log = LoggerFactory.getLogger(MinioServiceImpl.class);
    private static final int PRESIGNED_URL_EXPIRY_HOURS = 24;

    private final MinioClient minioClient;
    private final String bucketName;

    public MinioServiceImpl(MinioClient minioClient,
                            @Value("${minio.bucket-name}") String bucketName) {
        this.minioClient = minioClient;
        this.bucketName = bucketName;
    }

    @Override
    public String uploadFile(MultipartFile file, String path) {
        try {
            ensureBucketExists();
            
            String objectName = buildObjectName(path, file.getOriginalFilename());
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(file.getInputStream(), file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );
            
            log.info("File uploaded successfully");
            return getFileUrl(objectName);
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new MinioOperationException("Failed to upload file to Minio", e);
        }
    }

    @Override
    public String uploadFile(InputStream inputStream, String objectName, String contentType, long size) {
        try {
            ensureBucketExists();
            
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, size, -1)
                            .contentType(contentType)
                            .build()
            );
            
            log.info("File uploaded successfully");
            return getFileUrl(objectName);
            
        } catch (Exception e) {
            log.error("Failed to upload file: {}", e.getMessage());
            throw new MinioOperationException("Failed to upload file to Minio", e);
        }
    }

    @Override
    public String getFileUrl(String path) {
        try {
            String url = minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucketName)
                            .object(path)
                            .expiry(PRESIGNED_URL_EXPIRY_HOURS, TimeUnit.HOURS)
                            .build()
            );
            
            log.info("Generated presigned URL");
            return url;
            
        } catch (Exception e) {
            log.error("Failed to generate file URL: {}", e.getMessage());
            throw new MinioOperationException("Failed to generate file URL from Minio", e);
        }
    }

    @Override
    public InputStream downloadFile(String path) {
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            
            log.info("File downloaded successfully");
            return stream;
            
        } catch (Exception e) {
            log.error("Failed to download file: {}", e.getMessage());
            throw new MinioOperationException("Failed to download file from Minio", e);
        }
    }

    @Override
    public void deleteFile(String path) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            
            log.info("File deleted successfully");
            
        } catch (Exception e) {
            log.error("Failed to delete file: {}", e.getMessage());
            throw new MinioOperationException("Failed to delete file from Minio", e);
        }
    }

    @Override
    public boolean fileExists(String path) {
        try {
            minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(path)
                            .build()
            );
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void ensureBucketExists() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder()
                            .bucket(bucketName)
                            .build()
            );
            
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder()
                                .bucket(bucketName)
                                .build()
                );
                log.info("Bucket created: {}", bucketName);
            }
        } catch (Exception e) {
            log.error("Failed to ensure bucket exists: {}", e.getMessage());
            throw new MinioOperationException("Failed to ensure bucket exists", e);
        }
    }

    private String buildObjectName(String path, String filename) {
        if (path == null || path.isEmpty()) {
            return filename;
        }
        String normalizedPath = path.endsWith("/") ? path : path + "/";
        return normalizedPath + filename;
    }
}
