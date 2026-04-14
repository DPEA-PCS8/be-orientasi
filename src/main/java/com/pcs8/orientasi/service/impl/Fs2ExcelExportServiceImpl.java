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
            String search, UUID bidangId, UUID skpaId, String status,
            Integer year, Integer startMonth, Integer endMonth,
            String userDepartment, boolean canSeeAll) {
        
        // Fetch all data (no pagination for export)
        Pageable pageable = PageRequest.of(0, Integer.MAX_VALUE);
        List<Fs2DocumentResponse> data = fs2Service.search(search, bidangId, skpaId, status, year, startMonth, endMonth, pageable, userDepartment, canSeeAll).getContent();
        
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Semua F.S.2");
            
            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dateStyle = createDateStyle(workbook);
            CellStyle hyperlinkStyle = createHyperlinkStyle(workbook);
            
            // Create header row
            String[] headers = {
                "No", "Nama Aplikasi", "Kode Aplikasi", "SKPA", 
                "Status Tahapan", "Target Pengujian", "Target Deployment", "Target Go Live",
                "Status", "Tanggal Pengajuan", "User Pembuat", "Dokumen FS2"
            };
            
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Fs2DocumentResponse item : data) {
                Row row = sheet.createRow(rowNum);
                
                createCell(row, 0, rowNum, dataStyle);
                createCell(row, 1, item.getNamaAplikasi(), dataStyle);
                createCell(row, 2, item.getKodeAplikasi(), dataStyle);
                createCell(row, 3, item.getNamaSkpa(), dataStyle);
                createCell(row, 4, formatStatusTahapan(item.getStatusTahapan()), dataStyle);
                createCell(row, 5, formatMonthYear(item.getTargetPengujian()), dateStyle);
                createCell(row, 6, formatMonthYear(item.getTargetDeployment()), dateStyle);
                createCell(row, 7, formatMonthYear(item.getTargetGoLive()), dateStyle);
                createCell(row, 8, formatStatus(item.getStatus()), dataStyle);
                createCell(row, 9, formatDate(item.getTanggalPengajuan()), dateStyle);
                createCell(row, 10, item.getUserName(), dataStyle);
                createHyperlinkCell(workbook, row, 11, item.getDokumenPath(), hyperlinkStyle);
                
                rowNum++;
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
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
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
            
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
            
            // Create header row
            String[] headers = {
                "No", "Nama Aplikasi", "Kode Aplikasi", "Bidang", "SKPA",
                "Progres", "Status Progres", "Tanggal Progres",
                "Fase Pengajuan", "IKU", "Mekanisme", "Pelaksanaan",
                "PIC", "Anggota Tim",
                "Nomor ND", "Tanggal Berkas ND",
                "Nomor CD", "Tanggal Berkas CD Prinsip Persetujuan FS2",
                "Target Pengujian", "Realisasi Pengujian",
                "Target Deployment", "Realisasi Deployment",
                "Target Go Live", "Keterangan",
                "Berkas ND", "Berkas F.S.2", "Tgl Berkas FS2",
                "Berkas CD Prinsip", "Berkas F.S.2A", "Tgl Berkas FS2A",
                "Berkas F.S.2B", "Tgl Berkas FS2B",
                "Berkas F45", "Tgl Berkas F45", "Berkas F46", "Tgl Berkas F46",
                "Berkas ND/BA", "Tgl Berkas NDBA"
            };
            
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }
            
            // Create data rows
            int rowNum = 1;
            for (Fs2DocumentResponse item : data) {
                Row row = sheet.createRow(rowNum);
                
                createCell(row, 0, rowNum, dataStyle);
                createCell(row, 1, item.getNamaAplikasi(), dataStyle);
                createCell(row, 2, item.getKodeAplikasi(), dataStyle);
                createCell(row, 3, item.getNamaBidang(), dataStyle);
                createCell(row, 4, item.getNamaSkpa(), dataStyle);
                createCell(row, 5, formatProgres(item.getProgres()), dataStyle);
                createCell(row, 6, formatProgresStatus(item.getProgresStatus()), dataStyle);
                createCell(row, 7, formatDate(item.getTanggalProgres()), dateStyle);
                createCell(row, 8, formatFasePengajuan(item.getFasePengajuan()), dataStyle);
                createCell(row, 9, formatIku(item.getIku()), dataStyle);
                createCell(row, 10, formatMekanisme(item.getMekanisme()), dataStyle);
                createCell(row, 11, formatPelaksanaan(item), dataStyle);
                createCell(row, 12, item.getPicName(), dataStyle);
                createCell(row, 13, item.getAnggotaTimNames(), dataStyle);
                createCell(row, 14, item.getNomorNd(), dataStyle);
                createCell(row, 15, formatDate(item.getTanggalNd()), dateStyle);
                createCell(row, 16, item.getNomorCd(), dataStyle);
                createCell(row, 17, formatDate(item.getTanggalCd()), dateStyle);
                createCell(row, 18, formatMonthYear(item.getTargetPengujian()), dateStyle);
                createCell(row, 19, formatMonthYear(item.getRealisasiPengujian()), dateStyle);
                createCell(row, 20, formatMonthYear(item.getTargetDeployment()), dateStyle);
                createCell(row, 21, formatMonthYear(item.getRealisasiDeployment()), dateStyle);
                createCell(row, 22, formatMonthYear(item.getTargetGoLive()), dateStyle);
                createCell(row, 23, item.getKeterangan(), dataStyle);
                createHyperlinkCell(workbook, row, 24, item.getBerkasNd(), hyperlinkStyle);
                createHyperlinkCell(workbook, row, 25, item.getBerkasFs2(), hyperlinkStyle);
                createCell(row, 26, formatDate(item.getTanggalBerkasFs2()), dateStyle);
                createHyperlinkCell(workbook, row, 27, item.getBerkasCd(), hyperlinkStyle);
                createHyperlinkCell(workbook, row, 28, item.getBerkasFs2a(), hyperlinkStyle);
                createCell(row, 29, formatDate(item.getTanggalBerkasFs2a()), dateStyle);
                createHyperlinkCell(workbook, row, 30, item.getBerkasFs2b(), hyperlinkStyle);
                createCell(row, 31, formatDate(item.getTanggalBerkasFs2b()), dateStyle);
                createHyperlinkCell(workbook, row, 32, item.getBerkasF45(), hyperlinkStyle);
                createCell(row, 33, formatDate(item.getTanggalBerkasF45()), dateStyle);
                createHyperlinkCell(workbook, row, 34, item.getBerkasF46(), hyperlinkStyle);
                createCell(row, 35, formatDate(item.getTanggalBerkasF46()), dateStyle);
                createHyperlinkCell(workbook, row, 36, item.getBerkasNdBaDeployment(), hyperlinkStyle);
                createCell(row, 37, formatDate(item.getTanggalBerkasNdBa()), dateStyle);
                
                rowNum++;
            }
            
            // Auto-size columns
            for (int i = 0; i < headers.length; i++) {
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
            
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;
            
        } catch (IOException e) {
            log.error("Error creating Excel file for approved F.S.2", e);
            throw new RuntimeException("Gagal membuat file Excel", e);
        }
    }
    
    // ==================== Helper Methods ====================
    
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
            case "DEPLOY_SELESAI" -> "Deploy Selesai";
            default -> progres;
        };
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
