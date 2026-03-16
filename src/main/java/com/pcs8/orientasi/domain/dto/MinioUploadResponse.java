package com.pcs8.orientasi.domain.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MinioUploadResponse {
    
    @JsonProperty("file_url")
    private String fileUrl;
    
    @JsonProperty("file_name")
    private String fileName;
    
    @JsonProperty("file_path")
    private String filePath;
}
