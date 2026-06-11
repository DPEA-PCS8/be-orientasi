package com.pcs8.orientasi.client.catalog.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogBaseResponse<T> {

    private String status;
    private String message;
    private T data;
}
