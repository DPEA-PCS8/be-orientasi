package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter;
import com.pcs8.orientasi.domain.dto.response.Fs2DocumentResponse;
import com.pcs8.orientasi.service.Fs2ExcelExportService;
import com.pcs8.orientasi.service.Fs2Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.common.usermodel.HyperlinkType;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class Fs2ExcelExportServiceImpl implements Fs2ExcelExportService {

    private final Fs2Service fs2Service;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_YEAR_FORMATTER = DateTimeFormatter.ofPattern("MMMM yyyy");

    @Override
        public ByteArrayOutputStream exportAllFs2ToExcel(
            String search, UUID bidangId, UUID aplikasiId, String statusTahapan, UUID skpaId, String status,
            Integer year, Integer startMonth, Integer endMonth,
            String userDepartment, boolean canSeeAll) {
        
        // Fetch all data (no pagination for export)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Fs2DocumentResponse> data = fs2Service.search(search, aplikasiId, bidangId, statusTahapan, skpaId, status, year, startMonth, endMonth, pageable, userDepartment, canSeeAll).getContent();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Semua F.S.2");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);
            
            // Create header row
            // Move 'Kode Aplikasi' after 'Nama Aplikasi' and place 'Nama FS2' after it
            String[] headers = {
                "No", "Nama Aplikasi", "Kode Aplikasi", "Nama FS2", "SKPA", "Bidang",
                "Status Tahapan", "Target Pengujian", "Target Deployment", "Target Go Live",
                "Status", "Tanggal Pengajuan", "User Pembuat", "Dokumen FS2"
            };
            
            createHeaderRow(sheet, headers, headerStyle);
            
            // Create data rows
            int rowNum = 1;
            for (Fs2DocumentResponse item : data) {
                Row row = sheet.createRow(rowNum);
                
                createCell(row, 0, rowNum, dataStyle);
                createCell(row, 1, item.getNamaAplikasi(), dataStyle);
                // Kode Aplikasi moved to column 2
                createCell(row, 2, item.getKodeAplikasi(), dataStyle);
                // Nama FS2 moved to column 3
                createCell(row, 3, item.getNamaFs2(), dataStyle);
                // SKPA: include kode_skpa if available, otherwise fall back to nama_skpa
                String skpaValue = "-";
                if (item.getKodeSkpa() != null && !item.getKodeSkpa().isBlank()) {
                    skpaValue = item.getKodeSkpa() + (item.getNamaSkpa() != null ? " - " + item.getNamaSkpa() : "");
                } else if (item.getNamaSkpa() != null && !item.getNamaSkpa().isBlank()) {
                    skpaValue = item.getNamaSkpa();
                }
                createCell(row, 4, skpaValue, dataStyle);
                // Bidang
                createCell(row, 5, item.getNamaBidang(), dataStyle);
                createCell(row, 6, formatStatusTahapan(item.getStatusTahapan()), dataStyle);
                createCell(row, 7, formatMonthYear(item.getTargetPengujian()), dateStyle);
                createCell(row, 8, formatMonthYear(item.getTargetDeployment()), dateStyle);
                createCell(row, 9, formatMonthYear(item.getTargetGoLive()), dateStyle);
                createCell(row, 10, formatStatus(item.getStatus()), dataStyle);
                createCell(row, 11, formatDate(item.getTanggalPengajuan()), dateStyle);
                createCell(row, 12, item.getUserName(), dataStyle);
                createHyperlinkCell(workbook, row, 13, item.getDokumenPath(), hyperlinkStyle);
                
                rowNum++;
            }
            
            // Auto-size columns
            autoSizeColumns(sheet, headers.length);

            // Ensure 'Nama FS2' column (index 3) is at least as wide as 'Nama Aplikasi' (index 1)
            try {
                int namaAplikasiCol = 1;
                int namaFs2Col = 3;
                int widthAplikasi = sheet.getColumnWidth(namaAplikasiCol);
                int widthFs2 = sheet.getColumnWidth(namaFs2Col);
                if (widthFs2 < widthAplikasi) {
                    sheet.setColumnWidth(namaFs2Col, widthAplikasi);
                }
            } catch (Exception ex) {
                // Non-fatal: fallback to defaults if anything goes wrong while adjusting widths
                log.warn("Failed to adjust Nama FS2 column width, continuing with auto-sized widths", ex);
            }

            // Apply custom width adjustments for 'Semua F.S.2' export per request
            try {
                sheet.setColumnWidth(0, 2000);   // No (narrow)
                sheet.setColumnWidth(2, 3500);   // Kode Aplikasi (narrow)
                sheet.setColumnWidth(3, 15000);  // Nama FS2 (widen)
            } catch (Exception ex) {
                log.warn("Failed to apply custom column widths for all FS2 export", ex);
            }

            return writeWorkbookToStream(workbook);
            
        } catch (IOException e) {
            log.error("Error creating Excel file for all F.S.2", e);
            throw new RuntimeException("Gagal membuat file Excel", e);
        }
    }

    @Override
    public ByteArrayOutputStream exportApprovedFs2ToExcel(
            String search, UUID bidangId, UUID skpaId, String progres,
            String fasePengajuan, String mekanisme, String pelaksanaan,
            Integer year, Integer startMonth, Integer endMonth,
            String userDepartment, boolean canSeeAll) {
        
        // Fetch all data (no pagination for export)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        Fs2ApprovedSearchFilter filter = Fs2ApprovedSearchFilter.builder()
                .search(search)
                .bidangId(bidangId)
                .skpaId(skpaId)
                .progres(progres)
                .fasePengajuan(fasePengajuan)
                .mekanisme(mekanisme)
                .pelaksanaan(pelaksanaan)
                .year(year)
                .startMonth(startMonth)
                .endMonth(endMonth)
                .build();
        List<Fs2DocumentResponse> data = fs2Service.searchApproved(filter, pageable, userDepartment, canSeeAll).getContent();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monitoring F.S.2");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);
            
            // Create header row for Monitoring F.S.2
            // Removed "Bidang" column and moved "Kode Aplikasi" after "Nama Aplikasi"
            String[] headers = {
                "No", "Nama Aplikasi", "Kode Aplikasi", "Nama FS2", "SKPA",
                "Progres", "Status Progres",
                "Fase Pengajuan", "IKU", "Mekanisme", "Pelaksanaan",
                "PIC", "Anggota Tim",
                "Nomor ND", "Nomor CD",
                "Target Pengujian", "Realisasi Pengujian",
                "Target Deployment", "Realisasi Deployment",
                "Target Go Live", "Keterangan",
                "Berkas ND", "Tanggal Berkas ND",
                "Berkas F.S.2", "Tgl Berkas FS2",
                "Berkas CD Prinsip", "Tanggal Berkas CD Prinsip Persetujuan FS2",
                "Berkas F.S.2A", "Tgl Berkas FS2A",
                "Berkas F.S.2B", "Tgl Berkas FS2B",
                "Berkas F45", "Tgl Berkas F45", "Berkas F46", "Tgl Berkas F46",
                "Berkas ND/BA", "Tgl Berkas NDBA"
            };
            
            createHeaderRow(sheet, headers, headerStyle);
            
            // Create data rows
            int rowNum = 1;
            for (Fs2DocumentResponse item : data) {
                Row row = sheet.createRow(rowNum);
                
                createCell(row, 0, rowNum, dataStyle);
                createCell(row, 1, item.getNamaAplikasi(), dataStyle);
                // Move Kode Aplikasi to after Nama Aplikasi (index 2)
                createCell(row, 2, item.getKodeAplikasi(), dataStyle);
                // Nama FS2 moves to index 3
                createCell(row, 3, item.getNamaFs2(), dataStyle);
                // SKPA: include kode_skpa if available, otherwise fall back to nama_skpa
                String skpaValue = "-";
                if (item.getKodeSkpa() != null && !item.getKodeSkpa().isBlank()) {
                    skpaValue = item.getKodeSkpa() + (item.getNamaSkpa() != null ? " - " + item.getNamaSkpa() : "");
                } else if (item.getNamaSkpa() != null && !item.getNamaSkpa().isBlank()) {
                    skpaValue = item.getNamaSkpa();
                }
                createCell(row, 4, skpaValue, dataStyle);
                // Derive progress display: prefer explicit progres field, otherwise infer from tahapan statuses
                createCell(row, 5, deriveProgresDisplay(item), dataStyle);
                createCell(row, 6, formatProgresStatus(item.getProgresStatus()), dataStyle);
                createCell(row, 7, formatFasePengajuan(item.getFasePengajuan()), dataStyle);
                createCell(row, 8, formatIku(item.getIku()), dataStyle);
                createCell(row, 9, formatMekanisme(item.getMekanisme()), dataStyle);
                createCell(row, 10, formatPelaksanaan(item), dataStyle);
                createCell(row, 11, item.getPicName(), dataStyle);
                createCell(row, 12, item.getAnggotaTimNames(), dataStyle);
                createCell(row, 13, item.getNomorNd(), dataStyle);
                createCell(row, 14, item.getNomorCd(), dataStyle);
                createCell(row, 15, formatMonthYear(item.getTargetPengujian()), dateStyle);
                createCell(row, 16, formatMonthYear(item.getRealisasiPengujian()), dateStyle);
                createCell(row, 17, formatMonthYear(item.getTargetDeployment()), dateStyle);
                createCell(row, 18, formatMonthYear(item.getRealisasiDeployment()), dateStyle);
                createCell(row, 19, formatMonthYear(item.getTargetGoLive()), dateStyle);
                createCell(row, 20, item.getKeterangan(), dataStyle);
                createHyperlinkCell(workbook, row, 21, item.getBerkasNd(), hyperlinkStyle);
                createCell(row, 22, formatDate(item.getTanggalNd()), dateStyle);
                createHyperlinkCell(workbook, row, 23, item.getBerkasFs2(), hyperlinkStyle);
                createCell(row, 24, formatDate(item.getTanggalBerkasFs2()), dateStyle);
                createHyperlinkCell(workbook, row, 25, item.getBerkasCd(), hyperlinkStyle);
                createCell(row, 26, formatDate(item.getTanggalCd()), dateStyle);
                createHyperlinkCell(workbook, row, 27, item.getBerkasFs2a(), hyperlinkStyle);
                createCell(row, 28, formatDate(item.getTanggalBerkasFs2a()), dateStyle);
                createHyperlinkCell(workbook, row, 29, item.getBerkasFs2b(), hyperlinkStyle);
                createCell(row, 30, formatDate(item.getTanggalBerkasFs2b()), dateStyle);
                createHyperlinkCell(workbook, row, 31, item.getBerkasF45(), hyperlinkStyle);
                createCell(row, 32, formatDate(item.getTanggalBerkasF45()), dateStyle);
                createHyperlinkCell(workbook, row, 33, item.getBerkasF46(), hyperlinkStyle);
                createCell(row, 34, formatDate(item.getTanggalBerkasF46()), dateStyle);
                createHyperlinkCell(workbook, row, 35, item.getBerkasNdBaDeployment(), hyperlinkStyle);
                createCell(row, 36, formatDate(item.getTanggalBerkasNdBa()), dateStyle);
                
                rowNum++;
            }
            
            // Auto-size columns
            autoSizeColumns(sheet, headers.length);

            // Ensure 'Nama FS2' column (index 2) is at least as wide as 'Nama Aplikasi' (index 1)
            try {
                int namaAplikasiCol = 1;
                int namaFs2Col = 2;
                int widthAplikasi = sheet.getColumnWidth(namaAplikasiCol);
                int widthFs2 = sheet.getColumnWidth(namaFs2Col);
                if (widthFs2 < widthAplikasi) {
                    sheet.setColumnWidth(namaFs2Col, widthAplikasi);
                }
            } catch (Exception ex) {
                log.warn("Failed to adjust Nama FS2 column width for approved export, continuing with auto-sized widths", ex);
            }

            // Apply custom column width adjustments per request:
            // - Make 'No' column narrower
            // - Reduce 'Kode Aplikasi' column width
            // - Expand 'Nama FS2' column significantly
            // - Reduce 'Tanggal Berkas CD Prinsip Persetujuan FS2' column width
            try {
                // Column indices based on headers array
                sheet.setColumnWidth(0, 2000);   // No
                sheet.setColumnWidth(2, 3500);   // Kode Aplikasi
                sheet.setColumnWidth(3, 15000);  // Nama FS2 (widen)
                sheet.setColumnWidth(26, 2000);  // Tanggal Berkas CD Prinsip Persetujuan FS2
            } catch (Exception ex) {
                log.warn("Failed to apply custom column widths for approved export", ex);
            }

            return writeWorkbookToStream(workbook);
            
        } catch (IOException e) {
            log.error("Error creating Excel file for approved F.S.2", e);
            throw new RuntimeException("Gagal membuat file Excel", e);
        }
    }
    
    // ==================== Helper Methods ====================
    
    /**
     * Create header row with styling
     */
    private void createHeaderRow(Sheet sheet, String[] headers, CellStyle headerStyle) {
        Row headerRow = sheet.createRow(0);
        headerRow.setHeightInPoints(25);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }
    }
    
    /**
     * Auto-size columns with min and max width constraints
     */
    private void autoSizeColumns(Sheet sheet, int columnCount) {
        for (int i = 0; i < columnCount; i++) {
            sheet.autoSizeColumn(i);
            // Set minimum width for readability
            if (sheet.getColumnWidth(i) < 3000) {
                sheet.setColumnWidth(i, 3000);
            }
            // Set maximum width to prevent very wide columns
            if (sheet.getColumnWidth(i) > 15000) {
                sheet.setColumnWidth(i, 15000);
            }
        }
    }
    
    /**
     * Write workbook to ByteArrayOutputStream
     */
    private ByteArrayOutputStream writeWorkbookToStream(Workbook workbook) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        workbook.write(outputStream);
        return outputStream;
    }
    
    private void createCell(Row row, int col, Object value, CellStyle style) {
        Cell cell = row.createCell(col);
        if (value == null) {
            cell.setCellValue("-");
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(style);
    }
    
    /**
     * Create a hyperlink cell for URLs
     */
    private void createHyperlinkCell(Workbook workbook, Row row, int col, String url, CellStyle hyperlinkStyle) {
        Cell cell = row.createCell(col);
        
        if (url == null || url.trim().isEmpty()) {
            cell.setCellValue("-");
            // Use regular data style for empty cells
            CellStyle dataStyle = createDataStyle(workbook);
            cell.setCellStyle(dataStyle);
        } else {
            // Create hyperlink
            CreationHelper creationHelper = workbook.getCreationHelper();
            Hyperlink hyperlink = creationHelper.createHyperlink(HyperlinkType.URL);
            hyperlink.setAddress(url);
            
            // Set cell value and hyperlink
            cell.setCellValue("Download File");
            cell.setHyperlink(hyperlink);
            cell.setCellStyle(hyperlinkStyle);
        }
    }
    
    private String formatDate(LocalDate date) {
        if (date == null) return "-";
        return date.format(DATE_FORMATTER);
    }
    
    private String formatMonthYear(LocalDate date) {
        if (date == null) return "-";
        return date.format(MONTH_YEAR_FORMATTER);
    }
    
    private String formatStatus(String status) {
        if (status == null) return "-";
        return switch (status.toUpperCase()) {
            case "PENDING" -> "Pending";
            case "DISETUJUI" -> "Disetujui";
            case "TIDAK_DISETUJUI" -> "Tidak Disetujui";
            default -> status;
        };
    }
    
    private String formatStatusTahapan(String status) {
        if (status == null) return "-";
        return switch (status.toUpperCase()) {
            case "DESAIN" -> "Desain";
            case "PEMELIHARAAN" -> "Pemeliharaan";
            default -> status;
        };
    }
    
    private String formatUrgensi(String urgensi) {
        if (urgensi == null) return "-";
        return switch (urgensi.toUpperCase()) {
            case "RENDAH" -> "Rendah";
            case "SEDANG" -> "Sedang";
            case "TINGGI" -> "Tinggi";
            default -> urgensi;
        };
    }
    
    private String formatProgres(String progres) {
        if (progres == null) return "-";
        return switch (progres.toUpperCase()) {
            case "ASESMEN" -> "Asesmen";
            case "CODING" -> "Coding";
            case "PDKK" -> "PDKK";
            case "DEPLOY_SELESAI" -> "Deploy";
            case "GO_LIVE", "GO-LIVE", "GO LIVE" -> "Go Live";
            case "PEMROGRAMAN" -> "Pemrograman";
            case "PENGUJIAN" -> "Pengujian";
            case "DEPLOYMENT" -> "Deployment";
            default -> progres;
        };
    }

    /**
     * Derive a human-friendly progres display value. Prefer explicit `progres` field,
     * otherwise infer the current tahapan from tahapan status fields.
     */
    private String deriveProgresDisplay(Fs2DocumentResponse item) {
        if (item == null) return "-";

        String progres = item.getProgres();
        if (progres != null && !progres.isBlank()) {
            return formatProgres(progres);
        }

        if (item.getTahapanStatusAsesmen() != null && !item.getTahapanStatusAsesmen().isBlank()) {
            return "Asesmen";
        }
        if (item.getTahapanStatusPemrograman() != null && !item.getTahapanStatusPemrograman().isBlank()) {
            return "Pemrograman";
        }
        if (item.getTahapanStatusPengujian() != null && !item.getTahapanStatusPengujian().isBlank()) {
            return "Pengujian";
        }
        if (item.getTahapanStatusDeployment() != null && !item.getTahapanStatusDeployment().isBlank()) {
            return "Deployment";
        }
        if (item.getTahapanStatusGoLive() != null && !item.getTahapanStatusGoLive().isBlank()) {
            return "Go Live";
        }

        return "-";
    }
    
    private String formatProgresStatus(String status) {
        if (status == null) return "-";
        return switch (status.toUpperCase()) {
            case "BELUM_DIMULAI" -> "Belum Dimulai";
            case "DALAM_PROSES" -> "Dalam Proses";
            case "SELESAI" -> "Selesai";
            default -> status;
        };
    }
    
    private String formatFasePengajuan(String fase) {
        if (fase == null) return "-";
        return switch (fase.toUpperCase()) {
            case "DESAIN" -> "Desain";
            case "PEMELIHARAAN" -> "Pemeliharaan";
            default -> fase;
        };
    }
    
    private String formatIku(String iku) {
        if (iku == null) return "-";
        return switch (iku.toUpperCase()) {
            case "Y" -> "Ya";
            case "T" -> "Tidak";
            default -> iku;
        };
    }
    
    private String formatMekanisme(String mekanisme) {
        if (mekanisme == null) return "-";
        return switch (mekanisme.toUpperCase()) {
            case "INHOUSE" -> "Inhouse";
            case "OUTSOURCE" -> "Outsource";
            default -> mekanisme;
        };
    }
    
    private String formatPelaksanaan(Fs2DocumentResponse item) {
        if (item.getPelaksanaan() == null) return "-";
        return switch (item.getPelaksanaan().toUpperCase()) {
            case "SINGLE_YEAR" -> "Single Year";
            case "MULTIYEARS" -> "Multiyears";
            default -> item.getPelaksanaan();
        };
    }
    
    private String formatTahunPelaksanaan(Fs2DocumentResponse item) {
        if (item.getPelaksanaan() == null) return "-";
        if ("SINGLE_YEAR".equalsIgnoreCase(item.getPelaksanaan())) {
            return item.getTahun() != null ? item.getTahun().toString() : "-";
        } else if ("MULTIYEARS".equalsIgnoreCase(item.getPelaksanaan())) {
            if (item.getTahunMulai() != null && item.getTahunSelesai() != null) {
                return item.getTahunMulai() + " - " + item.getTahunSelesai();
            }
        }
        return "-";
    }
    
    // ==================== Style Methods ====================
    
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.DARK_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
    
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(true);
        return style;
    }
    
    private CellStyle createDateStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.CENTER);
        return style;
    }
    
    /**
     * Create style for hyperlinks (blue and underlined)
     */
    private CellStyle createHyperlinkStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.BLUE.getIndex());
        font.setUnderline(Font.U_SINGLE);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setWrapText(false);
        return style;
    }
}
