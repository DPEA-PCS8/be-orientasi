package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.UserProfileResponse;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfileServiceImpl.class);
    
    private final MstUserRepository mstUserRepository;
    
    @Override
    @Transactional(readOnly = true)
    public UserProfileResponse getUserProfileByUuid(String uuid) {
        logger.info("Fetching user profile for uuid: {}", uuid);
        
        UUID userUuid = UUID.fromString(uuid);
        
        MstUser user = mstUserRepository.findById(userUuid)
                .orElseThrow(() -> {
                    logger.warn("User not found with uuid: {}", uuid);
                    return new ResourceNotFoundException("User not found with uuid: " + uuid);
                });
        
        return UserProfileResponse.builder()
                .uuid(user.getUuid().toString())
                .fullName(user.getFullName())
                .departemen(user.getDepartment())
                .title(user.getTitle())
                .email(user.getEmail())
                .pksiList(new ArrayList<>())
                .build();
    }
}
