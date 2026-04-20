package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.PksiTimelineDto;
import com.pcs8.orientasi.domain.dto.request.PksiDocumentRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDocumentResponse;
import com.pcs8.orientasi.domain.entity.MstAplikasi;
import com.pcs8.orientasi.domain.entity.MstTeam;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.PksiTimeline;
import org.mapstruct.*;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * MapStruct mapper untuk konversi antara PksiDocument entity, request, dan response.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface PksiDocumentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    @Mapping(target = "aplikasi", ignore = true)
    @Mapping(target = "inisiatifGroup", ignore = true)
    @Mapping(target = "inisiatif", ignore = true)
    @Mapping(target = "team", ignore = true)
    @Mapping(target = "timelines", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "parentPksi", ignore = true)
    @Mapping(target = "childPksiList", ignore = true)
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
    @Mapping(target = "picSatker", expression = "java(document.getEffectivePicSatker())")
    @Mapping(target = "picSatkerBA", expression = "java(document.getEffectivePicSatker())")
    @Mapping(target = "timelines", expression = "java(convertTimelinesToDto(document.getEffectiveTimelines()))")
    @Mapping(target = "progress", expression = "java(document.getEffectiveProgress())")
    @Mapping(target = "iku", expression = "java(document.getEffectiveIku())")
    @Mapping(target = "inhouseOutsource", expression = "java(document.getEffectiveInhouseOutsource())")
    @Mapping(target = "picApproval", expression = "java(document.getEffectivePicApproval())")
    @Mapping(target = "picApprovalName", expression = "java(document.getEffectivePicApprovalName())")
    @Mapping(target = "anggotaTim", expression = "java(document.getEffectiveAnggotaTim())")
    @Mapping(target = "anggotaTimNames", expression = "java(document.getEffectiveAnggotaTimNames())")
    @Mapping(target = "anggaranTotal", expression = "java(document.getEffectiveAnggaranTotal())")
    @Mapping(target = "anggaranTahunIni", expression = "java(document.getEffectiveAnggaranTahunIni())")
    @Mapping(target = "anggaranTahunDepan", expression = "java(document.getEffectiveAnggaranTahunDepan())")
    @Mapping(target = "tanggalPengadaan", expression = "java(document.getEffectiveTanggalPengadaan())")
    @Mapping(target = "tanggalDesain", expression = "java(document.getEffectiveTanggalDesain())")
    @Mapping(target = "tanggalCoding", expression = "java(document.getEffectiveTanggalCoding())")
    @Mapping(target = "tanggalUnitTest", expression = "java(document.getEffectiveTanggalUnitTest())")
    @Mapping(target = "statusT01T02", expression = "java(document.getEffectiveStatusT01T02())")
    @Mapping(target = "berkasT01T02", expression = "java(document.getEffectiveBerkasT01T02())")
    @Mapping(target = "statusT11", expression = "java(document.getEffectiveStatusT11())")
    @Mapping(target = "berkasT11", expression = "java(document.getEffectiveBerkasT11())")
    @Mapping(target = "statusCd", expression = "java(document.getEffectiveStatusCd())")
    @Mapping(target = "nomorCd", expression = "java(document.getEffectiveNomorCd())")
    @Mapping(target = "kontrakTanggalMulai", expression = "java(document.getEffectiveKontrakTanggalMulai())")
    @Mapping(target = "kontrakTanggalSelesai", expression = "java(document.getEffectiveKontrakTanggalSelesai())")
    @Mapping(target = "kontrakNilai", expression = "java(document.getEffectiveKontrakNilai())")
    @Mapping(target = "kontrakJumlahTermin", expression = "java(document.getEffectiveKontrakJumlahTermin())")
    @Mapping(target = "kontrakDetailPembayaran", expression = "java(document.getEffectiveKontrakDetailPembayaran())")
    @Mapping(target = "baDeploy", expression = "java(document.getEffectiveBaDeploy())")
    @Mapping(target = "tahapanStatusUsreq", expression = "java(document.getEffectiveTahapanStatusUsreq())")
    @Mapping(target = "tahapanStatusPengadaan", expression = "java(document.getEffectiveTahapanStatusPengadaan())")
    @Mapping(target = "tahapanStatusDesain", expression = "java(document.getEffectiveTahapanStatusDesain())")
    @Mapping(target = "tahapanStatusCoding", expression = "java(document.getEffectiveTahapanStatusCoding())")
    @Mapping(target = "tahapanStatusUnitTest", expression = "java(document.getEffectiveTahapanStatusUnitTest())")
    @Mapping(target = "tahapanStatusSit", expression = "java(document.getEffectiveTahapanStatusSit())")
    @Mapping(target = "tahapanStatusUat", expression = "java(document.getEffectiveTahapanStatusUat())")
    @Mapping(target = "tahapanStatusDeployment", expression = "java(document.getEffectiveTahapanStatusDeployment())")
    @Mapping(target = "tahapanStatusSelesai", expression = "java(document.getEffectiveTahapanStatusSelesai())")
    @Mapping(target = "status", expression = "java(document.getStatus() != null ? document.getStatus().name() : null)")
    @Mapping(target = "createdAt", expression = "java(document.getCreatedAt() != null ? document.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : null)")
    @Mapping(target = "isNestedPksi", expression = "java(document.isNestedPksi())")
    @Mapping(target = "parentPksiId", expression = "java(extractParentPksiId(document))")
    @Mapping(target = "parentPksiNama", expression = "java(extractParentPksiNama(document))")
    @Mapping(target = "childCount", expression = "java(extractChildCount(document))")
    @Mapping(target = "childPksiList", expression = "java(extractChildPksiList(document))")
    PksiDocumentResponse mapToResponseForMonitoring(PksiDocument document);

    /**
     * Map PKSI Document to Response WITHOUT effective methods (for "Semua PKSI" page).
     * Returns original data as submitted, even for nested PKSI.
     */
    @Mapping(target = "id", expression = "java(document.getId().toString())")
    @Mapping(target = "userId", expression = "java(extractUserId(document))")
    @Mapping(target = "userName", expression = "java(extractUserName(document))")
    @Mapping(target = "aplikasiId", expression = "java(extractAplikasiIdOriginal(document))")
    @Mapping(target = "namaAplikasi", expression = "java(extractNamaAplikasiOriginal(document))")
    @Mapping(target = "inisiatifGroupId", expression = "java(extractInisiatifGroupId(document))")
    @Mapping(target = "inisiatifId", expression = "java(extractInisiatifId(document))")
    @Mapping(target = "inisiatifNomor", expression = "java(extractInisiatifNomor(document))")
    @Mapping(target = "inisiatifNama", expression = "java(extractInisiatifNama(document))")
    @Mapping(target = "inisiatifTahun", expression = "java(extractInisiatifTahun(document))")
    @Mapping(target = "teamId", expression = "java(extractTeamIdOriginal(document))")
    @Mapping(target = "teamName", expression = "java(extractTeamNameOriginal(document))")
    @Mapping(target = "tanggalPengajuan", expression = "java(formatDate(document.getTanggalPengajuan()))")
    @Mapping(target = "kapanHarusDiselesaikan", source = "kapanDiselesaikan")
    @Mapping(target = "picSatkerBA", source = "picSatker")
    @Mapping(target = "timelines", expression = "java(convertTimelinesToDto(document.getTimelines()))")
    @Mapping(target = "progress", source = "progress")
    @Mapping(target = "iku", source = "iku")
    @Mapping(target = "inhouseOutsource", source = "inhouseOutsource")
    @Mapping(target = "picApproval", source = "picApproval")
    @Mapping(target = "picApprovalName", source = "picApprovalName")
    @Mapping(target = "anggotaTim", source = "anggotaTim")
    @Mapping(target = "anggotaTimNames", source = "anggotaTimNames")
    @Mapping(target = "anggaranTotal", source = "anggaranTotal")
    @Mapping(target = "anggaranTahunIni", source = "anggaranTahunIni")
    @Mapping(target = "anggaranTahunDepan", source = "anggaranTahunDepan")
    @Mapping(target = "tanggalPengadaan", source = "tanggalPengadaan")
    @Mapping(target = "tanggalDesain", source = "tanggalDesain")
    @Mapping(target = "tanggalCoding", source = "tanggalCoding")
    @Mapping(target = "tanggalUnitTest", source = "tanggalUnitTest")
    @Mapping(target = "statusT01T02", source = "statusT01T02")
    @Mapping(target = "berkasT01T02", source = "berkasT01T02")
    @Mapping(target = "statusT11", source = "statusT11")
    @Mapping(target = "berkasT11", source = "berkasT11")
    @Mapping(target = "statusCd", source = "statusCd")
    @Mapping(target = "nomorCd", source = "nomorCd")
    @Mapping(target = "kontrakTanggalMulai", source = "kontrakTanggalMulai")
    @Mapping(target = "kontrakTanggalSelesai", source = "kontrakTanggalSelesai")
    @Mapping(target = "kontrakNilai", source = "kontrakNilai")
    @Mapping(target = "kontrakJumlahTermin", source = "kontrakJumlahTermin")
    @Mapping(target = "kontrakDetailPembayaran", source = "kontrakDetailPembayaran")
    @Mapping(target = "baDeploy", source = "baDeploy")
    @Mapping(target = "tahapanStatusUsreq", source = "tahapanStatusUsreq")
    @Mapping(target = "tahapanStatusPengadaan", source = "tahapanStatusPengadaan")
    @Mapping(target = "tahapanStatusDesain", source = "tahapanStatusDesain")
    @Mapping(target = "tahapanStatusCoding", source = "tahapanStatusCoding")
    @Mapping(target = "tahapanStatusUnitTest", source = "tahapanStatusUnitTest")
    @Mapping(target = "tahapanStatusSit", source = "tahapanStatusSit")
    @Mapping(target = "tahapanStatusUat", source = "tahapanStatusUat")
    @Mapping(target = "tahapanStatusDeployment", source = "tahapanStatusDeployment")
    @Mapping(target = "tahapanStatusSelesai", source = "tahapanStatusSelesai")
    @Mapping(target = "status", expression = "java(document.getStatus() != null ? document.getStatus().name() : null)")
    @Mapping(target = "createdAt", expression = "java(document.getCreatedAt() != null ? document.getCreatedAt().toString() : null)")
    @Mapping(target = "updatedAt", expression = "java(document.getUpdatedAt() != null ? document.getUpdatedAt().toString() : null)")
    @Mapping(target = "isNestedPksi", expression = "java(document.isNestedPksi())")
    @Mapping(target = "parentPksiId", expression = "java(extractParentPksiId(document))")
    @Mapping(target = "parentPksiNama", expression = "java(extractParentPksiNama(document))")
    @Mapping(target = "childCount", expression = "java(extractChildCount(document))")
    @Mapping(target = "childPksiList", expression = "java(extractChildPksiList(document))")
    PksiDocumentResponse mapToResponseOriginal(PksiDocument document);

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
        MstAplikasi aplikasi = document.getEffectiveAplikasi();
        if (aplikasi != null && aplikasi.getId() != null) {
            return aplikasi.getId().toString();
        }
        return null;
    }

    default String extractNamaAplikasi(PksiDocument document) {
        MstAplikasi aplikasi = document.getEffectiveAplikasi();
        if (aplikasi != null) {
            return aplikasi.getNamaAplikasi();
        }
        return null;
    }

    /**
     * Extract aplikasi ID from original data (without effective methods).
     * Used for "Semua PKSI" page to show data as submitted.
     */
    default String extractAplikasiIdOriginal(PksiDocument document) {
        if (document.getAplikasi() != null && document.getAplikasi().getId() != null) {
            return document.getAplikasi().getId().toString();
        }
        return null;
    }

    /**
     * Extract aplikasi name from original data (without effective methods).
     * Used for "Semua PKSI" page to show data as submitted.
     */
    default String extractNamaAplikasiOriginal(PksiDocument document) {
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
        MstTeam team = document.getEffectiveTeam();
        if (team != null && team.getId() != null) {
            return team.getId().toString();
        }
        return null;
    }

    default String extractTeamName(PksiDocument document) {
        MstTeam team = document.getEffectiveTeam();
        if (team != null) {
            return team.getName();
        }
        return null;
    }

    /**
     * Extract team ID from original data (without effective methods).
     * Used for "Semua PKSI" page to show data as submitted.
     */
    default String extractTeamIdOriginal(PksiDocument document) {
        if (document.getTeam() != null && document.getTeam().getId() != null) {
            return document.getTeam().getId().toString();
        }
        return null;
    }

    /**
     * Extract team name from original data (without effective methods).
     * Used for "Semua PKSI" page to show data as submitted.
     */
    default String extractTeamNameOriginal(PksiDocument document) {
        if (document.getTeam() != null) {
            return document.getTeam().getName();
        }
        return null;
    }

    /**
     * Convert timeline entities to DTOs (uses effective timelines for nested PKSI)
     */
    default List<PksiTimelineDto> convertTimelinesToDto(List<PksiTimeline> timelines) {
        if (timelines == null || timelines.isEmpty()) {
            return null;
        }
        return timelines.stream()
                .map(this::timelineToDto)
                .collect(Collectors.toList());
    }

    /**
     * Convert single timeline entity to DTO
     */
    default PksiTimelineDto timelineToDto(PksiTimeline timeline) {
        if (timeline == null) {
            return null;
        }
        return PksiTimelineDto.builder()
                .phase(timeline.getPhase())
                .targetDate(formatDate(timeline.getTargetDate()))
                .stage(timeline.getStage() != null ? timeline.getStage().name() : null)
                .build();
    }

    /**
     * Convert timeline DTOs to entities
     * Note: pksiDocument reference must be set separately by the service
     */
    default List<PksiTimeline> convertDtoToTimelines(List<PksiTimelineDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return null;
        }
        return dtos.stream()
                .map(this::dtoToTimeline)
                .collect(Collectors.toList());
    }

    /**
     * Convert single DTO to timeline entity
     */
    default PksiTimeline dtoToTimeline(PksiTimelineDto dto) {
        if (dto == null) {
            return null;
        }
        return PksiTimeline.builder()
                .phase(dto.getPhase())
                .targetDate(parseDate(dto.getTargetDate()))
                .stage(dto.getStage() != null ? PksiTimeline.TimelineStage.valueOf(dto.getStage()) : null)
                .build();
    }

    // ==================== NESTED PKSI HELPER METHODS ====================

    /**
     * Extract parent PKSI ID if exists
     */
    default String extractParentPksiId(PksiDocument document) {
        if (document.getParentPksi() != null && document.getParentPksi().getId() != null) {
            return document.getParentPksi().getId().toString();
        }
        return null;
    }

    /**
     * Extract parent PKSI nama if exists
     */
    default String extractParentPksiNama(PksiDocument document) {
        if (document.getParentPksi() != null) {
            return document.getParentPksi().getNamaPksi();
        }
        return null;
    }

    /**
     * Extract count of child PKSI documents
     */
    default Integer extractChildCount(PksiDocument document) {
        if (document.getChildPksiList() != null) {
            return document.getChildPksiList().size();
        }
        return 0;
    }

    /**
     * Extract child PKSI list summary
     */
    default java.util.List<PksiDocumentResponse.ChildPksiSummary> extractChildPksiList(PksiDocument document) {
        if (document.getChildPksiList() == null || document.getChildPksiList().isEmpty()) {
            return new java.util.ArrayList<>();
        }

        return document.getChildPksiList().stream()
                .map(child -> PksiDocumentResponse.ChildPksiSummary.builder()
                        .id(child.getId().toString())
                        .namaPksi(child.getNamaPksi())
                        .status(child.getStatus() != null ? child.getStatus().name() : null)
                        .namaAplikasi(child.getAplikasi() != null ? child.getAplikasi().getNamaAplikasi() : null)
                        .tanggalPengajuan(formatDate(child.getTanggalPengajuan()))
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }
}
