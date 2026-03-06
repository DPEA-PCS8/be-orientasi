package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UrlRequest {
    @JsonProperty("url")
    @NotBlank(message = "URL is required")
    private String url;

    @JsonProperty("tipe_akses")
    private String tipeAkses;

    @JsonProperty("keterangan")
    private String keterangan;
}
