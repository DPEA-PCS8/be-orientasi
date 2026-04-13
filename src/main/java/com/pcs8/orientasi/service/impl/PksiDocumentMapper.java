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
    @Mapping(target = "inisiatifGroup", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "tanggalPengajuan", expression = "java(parseDate(request.getTanggalPengajuan()))")
    @Mapping(target = "kapanDiselesaikan", expression = "java(resolveKapanDiselesaikan(request))")
    @Mapping(target = "picSatker", expression = "java(resolvePicSatker(request))")
    void mapRequestToDocument(PksiDocumentRequest request, @MappingTarget PksiDocument document);

    @Mapping(target = "id", expression = "java(document.getId().toString())")
    @Mapping(target = "userId", expression = "java(extractUserId(document))")
    @Mapping(target = "userName", expression = "java(extractUserName(document))")
    @Mapping(target = "aplikasiId", expression = "java(extractAplikasiId(document))")
    @Mapping(target = "namaAplikasi", expression = "java(extractNamaAplikasi(document))")
    @Mapping(target = "inisiatifGroupId", expression = "java(extractInisiatifGroupId(document))")
    @Mapping(target = "inisiatifId", expression = "java(extractInisiatifId(document))")
    @Mapping(target = "inisiatifNomor", expression = "java(extractInisiatifNomor(document))")
    @Mapping(target = "inisiatifNama", expression = "java(extractInisiatifNama(document))")
    @Mapping(target = "inisiatifTahun", expression = "java(extractInisiatifTahun(document))")
    @Mapping(target = "teamId", expression = "java(extractTeamId(document))")
    @Mapping(target = "teamName", expression = "java(extractTeamName(document))")
    @Mapping(target = "tanggalPengajuan", expression = "java(formatDate(document.getTanggalPengajuan()))")
    @Mapping(target = "kapanHarusDiselesaikan", source = "kapanDiselesaikan")
    @Mapping(target = "picSatkerBA", source = "picSatker")
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

    default String extractInisiatifGroupId(PksiDocument document) {
        // Derive from inisiatif if available
        if (document.getInisiatif() != null && document.getInisiatif().getGroup() != null) {
            return document.getInisiatif().getGroup().getId().toString();
        }
        // Fallback to stored group id
        if (document.getInisiatifGroup() != null && document.getInisiatifGroup().getId() != null) {
            return document.getInisiatifGroup().getId().toString();
        }
        return null;
    }

    default String extractInisiatifId(PksiDocument document) {
        if (document.getInisiatif() != null && document.getInisiatif().getId() != null) {
            return document.getInisiatif().getId().toString();
        }
        return null;
    }

    default String extractInisiatifNomor(PksiDocument document) {
        if (document.getInisiatif() != null) {
            return document.getInisiatif().getNomorInisiatif();
        }
        return null;
    }

    default String extractInisiatifNama(PksiDocument document) {
        if (document.getInisiatif() != null) {
            return document.getInisiatif().getNamaInisiatif();
        }
        return null;
    }

    default Integer extractInisiatifTahun(PksiDocument document) {
        if (document.getInisiatif() != null) {
            return document.getInisiatif().getTahun();
        }
        return null;
    }

    default String extractTeamId(PksiDocument document) {
        if (document.getTeam() != null && document.getTeam().getId() != null) {
            return document.getTeam().getId().toString();
        }
        return null;
    }

    default String extractTeamName(PksiDocument document) {
        if (document.getTeam() != null) {
            return document.getTeam().getName();
        }
        return null;
    }
}
