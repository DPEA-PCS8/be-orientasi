package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.MyWorkResponse;
import com.pcs8.orientasi.service.MyWorkService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/me")
@RequiredArgsConstructor
public class MyWorkController {

    private final MyWorkService myWorkService;

    @GetMapping("/work")
    public ResponseEntity<BaseResponse> getMyWork() {
        MyWorkResponse response = myWorkService.getMyWork();
        return ResponseEntity.ok(new BaseResponse(200, "Success", response));
    }
}
