package com.pcs8.orientasi.domain.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {
    
    private String uuid;
    
    @JsonProperty("full_name")
    private String fullName;
    
    private String departemen;
    
    private String title;
    
    private String email;
    
    @JsonProperty("pksi_list")
    private List<Object> pksiList;
}
