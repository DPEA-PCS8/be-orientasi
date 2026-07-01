package com.pcs8.orientasi.client.objectstorage;

public record ObjectStorageApiResponse<T>(boolean success, String message, T data) {}
