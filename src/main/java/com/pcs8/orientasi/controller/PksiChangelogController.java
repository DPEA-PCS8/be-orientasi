package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.PksiChangelogResponse;
import com.pcs8.orientasi.service.PksiChangelogService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/pksi")
@RequiredArgsConstructor
@RequiresRole({"Admin", "Pengembang", "SKPA"})
public class PksiChangelogController {

    private static final Logger log = LoggerFactory.getLogger(PksiChangelogController.class);
    private static final String SUCCESS_MESSAGE = "Success";

    private final PksiChangelogService pksiChangelogService;

    /**
     * Get all changelogs for a specific PKSI document
     */
    @GetMapping("/{pksiId}/changelogs")
    public ResponseEntity<BaseResponse> getChangelogs(@PathVariable UUID pksiId) {
        log.info("Fetching changelogs for PKSI document: {}", pksiId);
        
        List<PksiChangelogResponse> changelogs = pksiChangelogService.getChangelogsByPksiId(pksiId);
        long totalCount = pksiChangelogService.countChangelogsByPksiId(pksiId);
        
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("changelogs", changelogs);
        responseData.put("total_count", totalCount);
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), SUCCESS_MESSAGE, responseData));
    }
}
