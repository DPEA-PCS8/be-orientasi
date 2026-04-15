package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.request.FormasiEfektifRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifDetailResponse;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifResponse;
import com.pcs8.orientasi.service.FormasiEfektifService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for Formasi Efektif feature
 * Handles dashboard, detail, and parameter management endpoints
 */
@RestController
@RequestMapping("/formasi-efektif")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class FormasiEfektifController {

    private final FormasiEfektifService formasiEfektifService;

    /**
     * Get dashboard data with summary calculations
     * 
     * @param tahun Optional year filter (defaults to current year)
     * @return Dashboard response with summary and developer list
     */
    @GetMapping("/dashboard")
    public ResponseEntity<BaseResponse> getDashboard(
            @RequestParam(required = false) Integer tahun) {
        
        FormasiEfektifRequest request = FormasiEfektifRequest.builder()
                .tahun(tahun)
                .build();
        
        FormasiEfektifResponse response = formasiEfektifService.getDashboardData(request);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                response
        ));
    }

    /**
     * Get detail data with calculation breakdowns
     * 
     * @param tahun Optional year filter (defaults to current year)
     * @return Detail response with PKSI/FS2 breakdowns
     */
    @GetMapping("/detail")
    public ResponseEntity<BaseResponse> getDetail(
            @RequestParam(required = false) Integer tahun) {
        
        FormasiEfektifRequest request = FormasiEfektifRequest.builder()
                .tahun(tahun)
                .build();
        
        FormasiEfektifDetailResponse response = formasiEfektifService.getDetailData(request);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                response
        ));
    }

    /**
     * Get configuration parameters
     * 
     * @return List of parameters for FORMASI_EFEKTIF category
     */
    @GetMapping("/parameters")
    public ResponseEntity<BaseResponse> getParameters() {
        List<FormasiEfektifResponse.ParameterItem> parameters = formasiEfektifService.getParameters();
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Success",
                parameters
        ));
    }

    /**
     * Update configuration parameters
     * 
     * @param parameters List of parameters to update
     * @return Updated parameters
     */
    @PutMapping("/parameters")
    @RequiresRole("admin")
    public ResponseEntity<BaseResponse> updateParameters(
            @RequestBody List<FormasiEfektifResponse.ParameterItem> parameters) {
        
        List<FormasiEfektifResponse.ParameterItem> updated = formasiEfektifService.updateParameters(parameters);
        
        return ResponseEntity.ok(new BaseResponse(
                HttpStatus.OK.value(),
                "Parameters updated successfully",
                updated
        ));
    }
}
