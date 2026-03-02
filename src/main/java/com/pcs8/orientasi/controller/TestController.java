package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test controller for development and testing purposes only.
 * This controller is only available in 'dev' and 'test' profiles.
 */
@RestController
@Profile({"dev", "test"})
public class TestController {

    @GetMapping("/hello")
    public ResponseEntity<BaseResponse> hello() {
        return ResponseEntity.ok(
                new BaseResponse(HttpStatus.OK.value(), "Success", "Hello, World!")
        );
    }
}
