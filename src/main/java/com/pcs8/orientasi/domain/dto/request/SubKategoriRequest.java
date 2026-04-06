package com.pcs8.orientasi.domain.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubKategoriRequest {

    @JsonProperty("kode")
    @NotBlank(message = "Kode is required")
    @Size(max = 20, message = "Kode max 20 characters")
    private String kode;

    @JsonProperty("nama")
    @NotBlank(message = "Nama is required")
    @Size(max = 255, message = "Nama max 255 characters")
    private String nama;

    @JsonProperty("category_code")
    @NotBlank(message = "Category code is required")
    @Size(max = 10, message = "Category code max 10 characters")
    private String categoryCode;

    @JsonProperty("category_name")
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name max 100 characters")
    private String categoryName;
}
