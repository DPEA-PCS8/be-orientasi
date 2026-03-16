package com.pcs8.orientasi.service;

import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;

public interface MinioService {
    
    /**
     * Upload file ke Minio storage
     * @param file file yang akan diupload
     * @param path path/folder tujuan di Minio
     * @return URL akses file
     */
    String uploadFile(MultipartFile file, String path);
    
    /**
     * Upload file dari InputStream ke Minio storage
     * @param inputStream input stream file
     * @param objectName nama object/file di Minio
     * @param contentType content type file
     * @param size ukuran file
     * @return URL akses file
     */
    String uploadFile(InputStream inputStream, String objectName, String contentType, long size);
    
    /**
     * Mendapatkan presigned URL untuk akses file
     * @param path path file di Minio
     * @return presigned URL
     */
    String getFileUrl(String path);
    
    /**
     * Download file dari Minio
     * @param path path file di Minio
     * @return InputStream file
     */
    InputStream downloadFile(String path);
    
    /**
     * Hapus file dari Minio
     * @param path path file di Minio
     */
    void deleteFile(String path);
    
    /**
     * Cek apakah file exists di Minio
     * @param path path file di Minio
     * @return true jika file ada
     */
    boolean fileExists(String path);
}
