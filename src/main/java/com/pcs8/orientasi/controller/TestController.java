package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @GetMapping("/hello" )
    public ResponseEntity<BaseResponse> hello() {
        return ResponseEntity.ok(
                new BaseResponse(200, "Success", "Hello, World Testt!!!")
        );
    }

}
