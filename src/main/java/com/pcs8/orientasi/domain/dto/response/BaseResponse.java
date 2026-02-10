package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse {
    
    @JsonProperty("status")
    private Integer status;
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("data")
    private Object data;
}