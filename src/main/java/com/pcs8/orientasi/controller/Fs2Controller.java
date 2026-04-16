package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.constant.ConstantVariable;
import com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter;
import com.pcs8.orientasi.domain.dto.request.Fs2DocumentRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.Fs2ChangelogResponse;
import com.pcs8.orientasi.domain.dto.response.Fs2DocumentResponse;
import com.pcs8.orientasi.service.Fs2ChangelogService;
import com.pcs8.orientasi.service.Fs2ExcelExportService;
import com.pcs8.orientasi.service.Fs2Service;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/fs2")
@RequiredArgsConstructor
@Slf4j
@RequiresRole({"admin", "pengembang", "skpa"})
public class Fs2Controller {

    private final Fs2Service fs2Service;
    private final Fs2ChangelogService fs2ChangelogService;
    private final Fs2ExcelExportService fs2ExcelExportService;

    /**
     * Build pagination response map from Page result
     */
    private Map<String, Object> buildPaginationResponse(Page<?> pageResult) {
        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent());
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        responseData.put("has_next", pageResult.hasNext());
        responseData.put("has_previous", pageResult.hasPrevious());
        return responseData;
    }

    @PostMapping
    public ResponseEntity<BaseResponse> create(@Valid @RequestBody Fs2DocumentRequest request) {
        Fs2DocumentResponse response = fs2Service.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(HttpStatus.CREATED.value(), "F.S.2 berhasil dibuat", response));
    }

    @GetMapping
    public ResponseEntity<BaseResponse> getAll() {
        List<Fs2DocumentResponse> responses = fs2Service.getAll();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    @GetMapping("/search")
    public ResponseEntity<BaseResponse> search(
            @RequestParam(required = false) String search,
            @RequestParam(name = "aplikasi_id", required = false) UUID aplikasiId,
            @RequestParam(name = "status_tahapan", required = false) String statusTahapan,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "start_month", required = false) Integer startMonth,
            @RequestParam(name = "end_month", required = false) Integer endMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Extract user info from request attributes (set by AuthorizationInterceptor)
        @SuppressWarnings("unchecked")
        Set<String> userRoles = (Set<String>) httpRequest.getAttribute("user_roles");
        String userDepartment = (String) httpRequest.getAttribute("department");
        
        log.info("F.S.2 Search - User Roles: {}, Department: {}, Year: {}, Month Range: {}-{}", userRoles, userDepartment, year, startMonth, endMonth);
        
        // Admin and Pengembang can see all F.S.2, SKPA role only sees matching department
        boolean canSeeAll = userRoles != null && userRoles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role) || "pengembang".equalsIgnoreCase(role));
        
        log.info("F.S.2 Search - canSeeAll: {}", canSeeAll);
        
        Page<Fs2DocumentResponse> pageResult = fs2Service.search(search, aplikasiId, statusTahapan, skpaId, status, year, startMonth, endMonth, pageable, userDepartment, canSeeAll);
        
        log.info("F.S.2 Search - Results count: {}", pageResult.getTotalElements());
        
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, buildPaginationResponse(pageResult)));
    }

    @GetMapping("/search/approved")
    public ResponseEntity<BaseResponse> searchApproved(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String progres,
            @RequestParam(name = "progres_status", required = false) String progresStatus,
            @RequestParam(name = "fase_pengajuan", required = false) String fasePengajuan,
            @RequestParam(required = false) String mekanisme,
            @RequestParam(required = false) String pelaksanaan,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "start_month", required = false) Integer startMonth,
            @RequestParam(name = "end_month", required = false) Integer endMonth,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest
    ) {
        Pageable pageable = PageRequest.of(page, size);
        
        // Extract user info from request attributes (set by AuthorizationInterceptor)
        @SuppressWarnings("unchecked")
        Set<String> userRoles = (Set<String>) httpRequest.getAttribute("user_roles");
        String userDepartment = (String) httpRequest.getAttribute("department");
        
        // Admin and Pengembang can see all F.S.2, SKPA role only sees matching department
        boolean canSeeAll = userRoles != null && userRoles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role) || "pengembang".equalsIgnoreCase(role));
        
        Fs2ApprovedSearchFilter filter = Fs2ApprovedSearchFilter.builder()
                .search(search)
                .bidangId(bidangId)
                .skpaId(skpaId)
                .progres(progres)
                .progresStatus(progresStatus)
                .fasePengajuan(fasePengajuan)
                .mekanisme(mekanisme)
                .pelaksanaan(pelaksanaan)
                .year(year)
                .startMonth(startMonth)
                .endMonth(endMonth)
                .build();
        Page<Fs2DocumentResponse> pageResult = fs2Service.searchApproved(filter, pageable, userDepartment, canSeeAll);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, buildPaginationResponse(pageResult)));
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> searchList(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status,
            HttpServletRequest httpRequest
    ) {
        // Extract user info from request attributes (set by AuthorizationInterceptor)
        @SuppressWarnings("unchecked")
        Set<String> userRoles = (Set<String>) httpRequest.getAttribute("user_roles");
        String userDepartment = (String) httpRequest.getAttribute("department");
        
        // Admin and Pengembang can see all F.S.2, SKPA role only sees matching department
        boolean canSeeAll = userRoles != null && userRoles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role) || "pengembang".equalsIgnoreCase(role));
        
        List<Fs2DocumentResponse> responses = fs2Service.searchList(search, bidangId, skpaId, status, userDepartment, canSeeAll);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getById(@PathVariable UUID id) {
        Fs2DocumentResponse response = fs2Service.getById(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> update(@PathVariable UUID id, @Valid @RequestBody Fs2DocumentRequest request) {
        Fs2DocumentResponse response = fs2Service.update(id, request);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "F.S.2 berhasil diupdate", response));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<BaseResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status
    ) {
        Fs2DocumentResponse response = fs2Service.updateStatus(id, status);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Status F.S.2 berhasil diupdate", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> delete(@PathVariable UUID id) {
        fs2Service.delete(id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "F.S.2 berhasil dihapus", null));
    }

    /**
     * Get changelogs for an FS2 document
     */
    @GetMapping("/{fs2Id}/changelogs")
    public ResponseEntity<BaseResponse> getChangelogs(@PathVariable UUID fs2Id) {
        List<Fs2ChangelogResponse> changelogs = fs2ChangelogService.getChangelogsByFs2Id(fs2Id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, changelogs));
    }

    /**
     * Get changelog count for an FS2 document
     */
    @GetMapping("/{fs2Id}/changelogs/count")
    public ResponseEntity<BaseResponse> getChangelogCount(@PathVariable UUID fs2Id) {
        long count = fs2ChangelogService.countChangelogsByFs2Id(fs2Id);
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, count));
    }

    /**
     * Export all F.S.2 documents to Excel
     */
    @GetMapping("/export")
    public ResponseEntity<byte[]> exportAllToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(name = "aplikasi_id", required = false) UUID aplikasiId,
            @RequestParam(name = "status_tahapan", required = false) String statusTahapan,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "start_month", required = false) Integer startMonth,
            @RequestParam(name = "end_month", required = false) Integer endMonth,
            HttpServletRequest httpRequest
    ) {
        // Extract user info from request attributes
        @SuppressWarnings("unchecked")
        Set<String> userRoles = (Set<String>) httpRequest.getAttribute("user_roles");
        String userDepartment = (String) httpRequest.getAttribute("department");
        
        boolean canSeeAll = userRoles != null && userRoles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role) || "pengembang".equalsIgnoreCase(role));
        
        ByteArrayOutputStream outputStream = fs2ExcelExportService.exportAllFs2ToExcel(
                search, aplikasiId, statusTahapan, skpaId, status, year, startMonth, endMonth, userDepartment, canSeeAll);
        
        String filename = "Semua_FS2_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }

    /**
     * Export approved F.S.2 documents (Monitoring) to Excel
     */
    @GetMapping("/approved/export")
    public ResponseEntity<byte[]> exportApprovedToExcel(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String progres,
            @RequestParam(name = "fase_pengajuan", required = false) String fasePengajuan,
            @RequestParam(required = false) String mekanisme,
            @RequestParam(required = false) String pelaksanaan,
            @RequestParam(required = false) Integer year,
            @RequestParam(name = "start_month", required = false) Integer startMonth,
            @RequestParam(name = "end_month", required = false) Integer endMonth,
            HttpServletRequest httpRequest
    ) {
        // Extract user info from request attributes
        @SuppressWarnings("unchecked")
        Set<String> userRoles = (Set<String>) httpRequest.getAttribute("user_roles");
        String userDepartment = (String) httpRequest.getAttribute("department");
        
        boolean canSeeAll = userRoles != null && userRoles.stream()
                .anyMatch(role -> "admin".equalsIgnoreCase(role) || "pengembang".equalsIgnoreCase(role));
        
        ByteArrayOutputStream outputStream = fs2ExcelExportService.exportApprovedFs2ToExcel(
                search, bidangId, skpaId, progres, fasePengajuan, mekanisme, pelaksanaan,
                year, startMonth, endMonth, userDepartment, canSeeAll);
        
        String filename = "Monitoring_FS2_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xlsx";
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", filename);
        headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");
        
        return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);
    }
}
