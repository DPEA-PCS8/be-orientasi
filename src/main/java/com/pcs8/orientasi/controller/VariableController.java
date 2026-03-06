package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.config.annotation.RequiresRole;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.entity.MstVariable;
import com.pcs8.orientasi.repository.MstVariableRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/arsitektur/variable")
@RequiredArgsConstructor
@RequiresRole({"admin", "pengembang"})
public class VariableController {

    private final MstVariableRepository variableRepository;

    @GetMapping
    public ResponseEntity<BaseResponse> getByKategori(@RequestParam String kategori) {
        List<VariableResponse> responses = variableRepository
            .findByKategoriAndIsActiveTrueOrderByUrutanAscNamaAsc(kategori)
            .stream()
            .map(this::mapToResponse)
            .toList();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    @GetMapping("/all")
    public ResponseEntity<BaseResponse> getAll() {
        List<VariableResponse> responses = variableRepository
            .findByIsActiveTrueOrderByKategoriAscUrutanAscNamaAsc()
            .stream()
            .map(this::mapToResponse)
            .toList();
        return ResponseEntity.ok(new BaseResponse(HttpStatus.OK.value(), "Success", responses));
    }

    private VariableResponse mapToResponse(MstVariable entity) {
        return new VariableResponse(
                entity.getId().toString(),
                entity.getKategori(),
                entity.getKode(),
                entity.getNama(),
                entity.getDeskripsi(),
                entity.getUrutan()
        );
    }

    public record VariableResponse(
            String id,
            String kategori,
            String kode,
            String nama,
            String deskripsi,
            Integer urutan
    ) {}
}
