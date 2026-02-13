package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.response.UserProfileResponse;

public interface UserProfileService {
    
    UserProfileResponse getUserProfileByUuid(String uuid);
}
