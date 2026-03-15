package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.constant.ConstantVariable;
import com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter;
import com.pcs8.orientasi.domain.dto.request.Fs2DocumentRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.Fs2DocumentResponse;
import com.pcs8.orientasi.service.Fs2Service;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/fs2")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class Fs2Controller {

    private final Fs2Service fs2Service;

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
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Fs2DocumentResponse> pageResult = fs2Service.search(search, bidangId, skpaId, status, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent());
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        responseData.put("has_next", pageResult.hasNext());
        responseData.put("has_previous", pageResult.hasPrevious());

        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responseData));
    }

    @GetMapping("/search/approved")
    public ResponseEntity<BaseResponse> searchApproved(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String progres,
            @RequestParam(name = "fase_pengajuan", required = false) String fasePengajuan,
            @RequestParam(required = false) String mekanisme,
            @RequestParam(required = false) String pelaksanaan,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Pageable pageable = PageRequest.of(page, size);
        Fs2ApprovedSearchFilter filter = Fs2ApprovedSearchFilter.builder()
                .search(search)
                .bidangId(bidangId)
                .skpaId(skpaId)
                .progres(progres)
                .fasePengajuan(fasePengajuan)
                .mekanisme(mekanisme)
                .pelaksanaan(pelaksanaan)
                .build();
        Page<Fs2DocumentResponse> pageResult = fs2Service.searchApproved(filter, pageable);

        Map<String, Object> responseData = new HashMap<>();
        responseData.put("content", pageResult.getContent());
        responseData.put("total_elements", pageResult.getTotalElements());
        responseData.put("total_pages", pageResult.getTotalPages());
        responseData.put("page", pageResult.getNumber());
        responseData.put("size", pageResult.getSize());
        responseData.put("has_next", pageResult.hasNext());
        responseData.put("has_previous", pageResult.hasPrevious());

        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), ConstantVariable.SUCCESS_MESSAGE, responseData));
    }

    @GetMapping("/list")
    public ResponseEntity<BaseResponse> searchList(
            @RequestParam(required = false) String search,
            @RequestParam(name = "bidang_id", required = false) UUID bidangId,
            @RequestParam(name = "skpa_id", required = false) UUID skpaId,
            @RequestParam(required = false) String status
    ) {
        List<Fs2DocumentResponse> responses = fs2Service.searchList(search, bidangId, skpaId, status);
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
}
