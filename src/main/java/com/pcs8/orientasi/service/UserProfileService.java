package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.UserProfileResponse;
import com.pcs8.orientasi.domain.dto.response.UserSimpleResponse;

import java.util.List;

public interface UserProfileService {
    
    UserProfileResponse getUserProfileByUuid(String uuid);
    
    List<UserSimpleResponse> getUsersByRole(String roleName);
}
