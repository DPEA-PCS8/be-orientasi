package com.pcs8.orientasi.client.objectstorage;

import com.pcs8.orientasi.client.sso.SsoTokenClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Slf4j
@Service
public class ObjectStorageClient implements FileStorageService {

    private final SsoTokenClient ssoTokenClient;
    private final RestClient restClient;

    @Value("${object-storage.base-url}")
    private String baseUrl;

    public ObjectStorageClient(SsoTokenClient ssoTokenClient, RestClient.Builder restClientBuilder) {
        this.ssoTokenClient = ssoTokenClient;
        this.restClient = restClientBuilder.build();
    }

    @Override
    public String uploadFile(MultipartFile file, String ownerType, String ownerId) {
        try {
            ByteArrayResource fileResource = new ByteArrayResource(file.getBytes()) {
                @Override
                public String getFilename() {
                    return file.getOriginalFilename();
                }
            };

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("ownerType", ownerType);
            body.add("ownerId", ownerId);
            body.add("accessLevel", "PRIVATE");

            ObjectStorageApiResponse<ObjectStorageFileResponse> response = restClient.post()
                    .uri(baseUrl + "/api/v1/files")
                    .header("Authorization", "Bearer " + ssoTokenClient.getToken())
                    .contentType(MediaType.MULTIPART_FORM_DATA)
                    .body(body)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || response.data() == null) {
                throw new RuntimeException("Empty response from object storage");
            }
            return response.data().id().toString();
        } catch (IOException e) {
            throw new RuntimeException("File storage operation failed", e);
        }
    }

    @Override
    public String getFileUrl(String storageId) {
        try {
            ObjectStorageApiResponse<ObjectStorageFileResponse> response = restClient.get()
                    .uri(baseUrl + "/api/v1/files/" + storageId)
                    .header("Authorization", "Bearer " + ssoTokenClient.getToken())
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (response == null || response.data() == null) {
                throw new RuntimeException("Empty response from object storage for id: " + storageId);
            }
            return response.data().url();
        } catch (Exception e) {
            throw new RuntimeException("File storage operation failed", e);
        }
    }

    @Override
    public byte[] downloadFile(String storageId) {
        String presignedUrl = getFileUrl(storageId);
        try {
            return restClient.get()
                    .uri(presignedUrl)
                    .retrieve()
                    .body(byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("File storage operation failed", e);
        }
    }

    @Override
    public void deleteFile(String storageId) {
        try {
            restClient.delete()
                    .uri(baseUrl + "/api/v1/files/" + storageId)
                    .header("Authorization", "Bearer " + ssoTokenClient.getToken())
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            throw new RuntimeException("File storage operation failed", e);
        }
    }

    @Override
    public boolean fileExists(String storageId) {
        try {
            restClient.get()
                    .uri(baseUrl + "/api/v1/files/" + storageId)
                    .header("Authorization", "Bearer " + ssoTokenClient.getToken())
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (HttpClientErrorException e) {
            return false;
        } catch (Exception e) {
            log.warn("Could not check file existence for {}: {}", storageId, e.getMessage());
            return false;
        }
    }
}
