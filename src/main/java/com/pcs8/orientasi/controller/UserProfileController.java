package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.UserProfileResponse;
import com.pcs8.orientasi.service.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserProfileController {
    
    private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);
    
    private final UserProfileService userProfileService;
    
    @GetMapping("/profile/{uuid}")
    public ResponseEntity<BaseResponse> getUserProfile(@PathVariable String uuid) {
        logger.info("GET /api/users/profile/{}", uuid);
        
        UserProfileResponse profile = userProfileService.getUserProfileByUuid(uuid);
        return ResponseEntity.ok(new BaseResponse(200, "Success", profile));
    }
}
