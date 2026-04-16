package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.entity.*;
import com.pcs8.orientasi.dto.PksiBulkInsertResponse;
import com.pcs8.orientasi.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PksiBulkInsertService {

    private final PksiDocumentRepository pksiDocumentRepository;
    private final PksiTimelineRepository pksiTimelineRepository;
    private final MstAplikasiRepository aplikasiRepository;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final TeamRepository teamRepository;
    private final MstUserRepository userRepository;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Process Excel file and bulk insert PKSI documents
     */
    @Transactional
    public PksiBulkInsertResponse processBulkInsert(MultipartFile file) throws IOException {
        PksiBulkInsertResponse response = PksiBulkInsertResponse.builder()
                .totalRows(0)
                .successCount(0)
                .failedCount(0)
                .errors(new ArrayList<>())
                .build();

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheetAt(0);
            int totalRows = sheet.getLastRowNum();
            response.setTotalRows(totalRows);

            // Skip header row (row 0)
            for (int i = 1; i <= totalRows; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                try {
                    processRow(row, i);
                    response.setSuccessCount(response.getSuccessCount() + 1);
                } catch (Exception e) {
                    log.error("Error processing row {}: {}", i, e.getMessage(), e);
                    response.setFailedCount(response.getFailedCount() + 1);
                    response.getErrors().add(PksiBulkInsertResponse.RowError.builder()
                            .rowNumber(i)
                            .errorMessage(e.getMessage())
                            .build());
                }
            }
        }

        return response;
    }

    /**
     * Process a single row and insert to database
     */
    private void processRow(Row row, int rowNumber) {
        // Read columns from Excel
        // Header: no	kode_aplikasi	nama_pksi	jenis_pksi	kode_bidang	kode_skpa	team_id	iku	inhouse_outsource
        //         timeline_phase_1_usreq	timeline_phase_1_sit	timeline_phase_1_uat	timeline_phase_1_go_live
        //         timeline_phase_2_usreq	timeline_phase_2_sit	timeline_phase_2_uat	timeline_phase_2_go_live

        String kodeAplikasi = getCellValueAsString(row.getCell(1));
        String namaPksi = getCellValueAsString(row.getCell(2));
        String jenisPksi = getCellValueAsString(row.getCell(3));
        String kodeBidang = getCellValueAsString(row.getCell(4));
        String kodeSkpa = getCellValueAsString(row.getCell(5));
        String teamIdStr = getCellValueAsString(row.getCell(6));
        String iku = getCellValueAsString(row.getCell(7));
        String inhouseOutsource = getCellValueAsString(row.getCell(8));

        // Timeline Phase 1
        String phase1Usreq = getCellValueAsString(row.getCell(9));
        String phase1Sit = getCellValueAsString(row.getCell(10));
        String phase1Uat = getCellValueAsString(row.getCell(11));
        String phase1GoLive = getCellValueAsString(row.getCell(12));

        // Timeline Phase 2
        String phase2Usreq = getCellValueAsString(row.getCell(13));
        String phase2Sit = getCellValueAsString(row.getCell(14));
        String phase2Uat = getCellValueAsString(row.getCell(15));
        String phase2GoLive = getCellValueAsString(row.getCell(16));

        // Validate required fields
        if (namaPksi == null || namaPksi.trim().isEmpty()) {
            throw new IllegalArgumentException("nama_pksi is required");
        }

        // Lookup relations
        MstAplikasi aplikasi = null;
        if (kodeAplikasi != null && !kodeAplikasi.trim().isEmpty()) {
            aplikasi = aplikasiRepository.findByKodeAplikasi(kodeAplikasi)
                    .orElseThrow(() -> new IllegalArgumentException("Aplikasi not found: " + kodeAplikasi));
        }

        // Validate bidang exists (for validation purposes only)
        if (kodeBidang != null && !kodeBidang.trim().isEmpty()) {
            bidangRepository.findByKodeBidang(kodeBidang)
                    .orElseThrow(() -> new IllegalArgumentException("Bidang not found: " + kodeBidang));
        }

        // Validate SKPA exists (for validation purposes only)
        String skpaId = null;
        if (kodeSkpa != null && !kodeSkpa.trim().isEmpty()) {
            skpaId = skpaRepository.findByKodeSkpa(kodeSkpa)
                    .orElseThrow(() -> new IllegalArgumentException("SKPA not found: " + kodeSkpa))
                    .getId().toString();
        }

        MstTeam team = null;
        if (teamIdStr != null && !teamIdStr.trim().isEmpty()) {
            try {
                UUID teamId = UUID.fromString(teamIdStr);
                team = teamRepository.findById(teamId)
                        .orElseThrow(() -> new IllegalArgumentException("Team not found: " + teamIdStr));
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Invalid team_id format: " + teamIdStr);
            }
        }

        // Get default user (for created_by)
        // Since we don't have user context in bulk insert, use first admin user or create a system user
        MstUser systemUser = userRepository.findByUsername("marvel.krent")
                .orElseGet(() -> {
                    // If no SYSTEM user exists, use first available user
                    return userRepository.findAll().stream()
                            .findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("No user available in system"));
                });

        // Build PksiDocument
        PksiDocument pksi = PksiDocument.builder()
                .jenisPksi(jenisPksi)
                .namaPksi(namaPksi)
                .aplikasi(aplikasi)
                .user(systemUser)
                .picSatker(skpaId)
                .team(team)
                .iku(iku)
                .inhouseOutsource(inhouseOutsource)
                .status(PksiDocument.DocumentStatus.DISETUJUI)
                .tanggalPengajuan(LocalDate.now())
                .timelines(new ArrayList<>())
                .build();

        // Save PKSI document first
        PksiDocument savedPksi = pksiDocumentRepository.save(pksi);

        // Build and save timelines
        List<PksiTimeline> timelines = new ArrayList<>();

        // Phase 1
        if (phase1Usreq != null && !phase1Usreq.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 1, PksiTimeline.TimelineStage.USREQ, parseDate(phase1Usreq)));
        }
        if (phase1Sit != null && !phase1Sit.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 1, PksiTimeline.TimelineStage.SIT, parseDate(phase1Sit)));
        }
        if (phase1Uat != null && !phase1Uat.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 1, PksiTimeline.TimelineStage.UAT, parseDate(phase1Uat)));
        }
        if (phase1GoLive != null && !phase1GoLive.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 1, PksiTimeline.TimelineStage.GO_LIVE, parseDate(phase1GoLive)));
        }

        // Phase 2
        if (phase2Usreq != null && !phase2Usreq.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 2, PksiTimeline.TimelineStage.USREQ, parseDate(phase2Usreq)));
        }
        if (phase2Sit != null && !phase2Sit.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 2, PksiTimeline.TimelineStage.SIT, parseDate(phase2Sit)));
        }
        if (phase2Uat != null && !phase2Uat.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 2, PksiTimeline.TimelineStage.UAT, parseDate(phase2Uat)));
        }
        if (phase2GoLive != null && !phase2GoLive.trim().isEmpty()) {
            timelines.add(createTimeline(savedPksi, 2, PksiTimeline.TimelineStage.GO_LIVE, parseDate(phase2GoLive)));
        }

        // Save all timelines
        if (!timelines.isEmpty()) {
            pksiTimelineRepository.saveAll(timelines);
        }

        log.info("Successfully inserted PKSI: {} (row {})", namaPksi, rowNumber);
    }

    /**
     * Create timeline entity
     */
    private PksiTimeline createTimeline(PksiDocument pksi, int phase, PksiTimeline.TimelineStage stage, LocalDate date) {
        return PksiTimeline.builder()
                .pksiDocument(pksi)
                .phase(phase)
                .stage(stage)
                .targetDate(date)
                .build();
    }

    /**
     * Parse date string to LocalDate
     */
    private LocalDate parseDate(String dateStr) {
        try {
            return LocalDate.parse(dateStr, DATE_FORMATTER);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid date format: " + dateStr + " (expected: yyyy-MM-dd)");
        }
    }

    /**
     * Get cell value as String, handling different cell types
     */
    private String getCellValueAsString(Cell cell) {
        if (cell == null) {
            return null;
        }

        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue().trim();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    LocalDate date = cell.getLocalDateTimeCellValue().toLocalDate();
                    return date.format(DATE_FORMATTER);
                } else {
                    // Handle numeric values (like team_id as number)
                    double numericValue = cell.getNumericCellValue();
                    // If it's a whole number, return without decimal
                    if (numericValue == Math.floor(numericValue)) {
                        return String.valueOf((long) numericValue);
                    }
                    return String.valueOf(numericValue);
                }
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return null;
        }
    }
}
