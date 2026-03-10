package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

/**
 * MapStruct mapper untuk konversi antara PksiDocument entity, request, dan response.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PksiDocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "aplikasi", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tanggalPengajuan", expression = "java(parseDate(request.getTanggalPengajuan()))")
    @Mapping(target = "kapanDiselesaikan", expression = "java(resolveKapanDiselesaikan(request))")
    @Mapping(target = "picSatker", expression = "java(resolvePicSatker(request))")
    @Mapping(target = "tahap1Awal", expression = "java(parseDate(request.getTahap1Awal()))")
    @Mapping(target = "tahap1Akhir", expression = "java(parseDate(request.getTahap1Akhir()))")
    @Mapping(target = "tahap5Awal", expression = "java(parseDate(request.getTahap5Awal()))")
    @Mapping(target = "tahap5Akhir", expression = "java(parseDate(request.getTahap5Akhir()))")
    @Mapping(target = "tahap7Awal", expression = "java(parseDate(request.getTahap7Awal()))")
    @Mapping(target = "tahap7Akhir", expression = "java(parseDate(request.getTahap7Akhir()))")
    void mapRequestToDocument(PksiDocumentRequest request, @MappingTarget PksiDocument document);

    @Mapping(target = "id", expression = "java(document.getId().toString())")
    @Mapping(target = "userId", expression = "java(extractUserId(document))")
    @Mapping(target = "userName", expression = "java(extractUserName(document))")
    @Mapping(target = "aplikasiId", expression = "java(extractAplikasiId(document))")
    @Mapping(target = "namaAplikasi", expression = "java(extractNamaAplikasi(document))")
    @Mapping(target = "tanggalPengajuan", expression = "java(formatDate(document.getTanggalPengajuan()))")
    @Mapping(target = "kapanHarusDiselesaikan", source = "kapanDiselesaikan")
    @Mapping(target = "picSatkerBA", source = "picSatker")
    @Mapping(target = "tahap1Awal", expression = "java(formatDate(document.getTahap1Awal()))")
    @Mapping(target = "tahap1Akhir", expression = "java(formatDate(document.getTahap1Akhir()))")
    @Mapping(target = "tahap5Awal", expression = "java(formatDate(document.getTahap5Awal()))")
    @Mapping(target = "tahap5Akhir", expression = "java(formatDate(document.getTahap5Akhir()))")
    @Mapping(target = "tahap7Awal", expression = "java(formatDate(document.getTahap7Awal()))")
    @Mapping(target = "tahap7Akhir", expression = "java(formatDate(document.getTahap7Akhir()))")
    @Mapping(target = "status", expression = "java(document.getStatus() != null ? document.getStatus().name() : null)")
    @Mapping(target = "createdAt", expression = "java(document.getCreatedAt() != null ? document.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : null)")
    PksiDocumentResponse mapToResponse(PksiDocument document);

    default LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDate.parse(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    default String formatDate(LocalDate date) {
        return date != null ? date.toString() : null;
    }

    default String resolveKapanDiselesaikan(PksiDocumentRequest request) {
        if (request.getKapanHarusDiselesaikan() != null && !request.getKapanHarusDiselesaikan().isEmpty()) {
            return request.getKapanHarusDiselesaikan();
        }
        return request.getKapanDiselesaikan();
    }

    default String resolvePicSatker(PksiDocumentRequest request) {
        if (request.getPicSatkerBA() != null && !request.getPicSatkerBA().isEmpty()) {
            return request.getPicSatkerBA();
        }
        return request.getPicSatker();
    }

    default String extractUserId(PksiDocument document) {
        if (document.getUser() != null && document.getUser().getUuid() != null) {
            return document.getUser().getUuid().toString();
        }
        return null;
    }

    default String extractUserName(PksiDocument document) {
        if (document.getUser() != null) {
            return document.getUser().getFullName();
        }
        return null;
    }

    default String extractAplikasiId(PksiDocument document) {
        if (document.getAplikasi() != null && document.getAplikasi().getId() != null) {
            return document.getAplikasi().getId().toString();
        }
        return null;
    }

    default String extractNamaAplikasi(PksiDocument document) {
        if (document.getAplikasi() != null) {
            return document.getAplikasi().getNamaAplikasi();
        }
        return null;
    }
}
