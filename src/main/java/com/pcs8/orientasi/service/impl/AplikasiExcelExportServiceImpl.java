package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.*;
import com.pcs8.orientasi.service.AplikasiExcelExportService;
import com.pcs8.orientasi.service.AplikasiHistorisService;
import com.pcs8.orientasi.service.AplikasiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AplikasiExcelExportServiceImpl implements AplikasiExcelExportService {

    private final AplikasiService aplikasiService;
    private final AplikasiHistorisService aplikasiHistorisService;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");
    private static final String LINE_BREAK = "\n";

    @Override
    public ByteArrayOutputStream exportAplikasiToExcel() {
        List<AplikasiResponse> aplikasiList = aplikasiService.getAll();

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Daftar Aplikasi");

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle wrapStyle = createWrapStyle(workbook);

            // Define column headers
            String[] headers = {
                "No", "Kode Aplikasi", "Nama Aplikasi", "Deskripsi", "Status Aplikasi",
                "Tanggal Status", "Kode Bidang", "Nama Bidang", "Kode SKPA", "Nama SKPA",
                "Tanggal Implementasi", "Akses", "Proses Data Pribadi", "Data Pribadi Diproses",
                "Kategori Idle", "Alasan Idle", "Rencana Pengakhiran", "Alasan Belum Diakhiri",
                "URLs", "Satker Internal", "Pengguna Eksternal", "Komunikasi Sistem", "Penghargaan"
            };

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (AplikasiResponse aplikasi : aplikasiList) {
                Row row = sheet.createRow(rowNum);
                int colNum = 0;

                // No
                createCell(row, colNum++, rowNum, dataStyle);
                
                // Kode Aplikasi
                createCell(row, colNum++, aplikasi.getKodeAplikasi(), dataStyle);
                
                // Nama Aplikasi
                createCell(row, colNum++, aplikasi.getNamaAplikasi(), dataStyle);
                
                // Deskripsi
                createCell(row, colNum++, aplikasi.getDeskripsi(), wrapStyle);
                
                // Status Aplikasi
                createCell(row, colNum++, aplikasi.getStatusAplikasi(), dataStyle);
                
                // Tanggal Status
                createCell(row, colNum++, formatDate(aplikasi.getTanggalStatus()), dataStyle);
                
                // Bidang
                BidangInfo bidang = aplikasi.getBidang();
                createCell(row, colNum++, bidang != null ? bidang.getKodeBidang() : "", dataStyle);
                createCell(row, colNum++, bidang != null ? bidang.getNamaBidang() : "", dataStyle);
                
                // SKPA
                SkpaInfo skpa = aplikasi.getSkpa();
                createCell(row, colNum++, skpa != null ? skpa.getKodeSkpa() : "", dataStyle);
                createCell(row, colNum++, skpa != null ? skpa.getNamaSkpa() : "", dataStyle);
                
                // Tanggal Implementasi
                createCell(row, colNum++, formatDate(aplikasi.getTanggalImplementasi()), dataStyle);
                
                // Akses
                createCell(row, colNum++, formatAkses(aplikasi.getAkses()), dataStyle);
                
                // Proses Data Pribadi
                createCell(row, colNum++, formatBoolean(aplikasi.getProsesDataPribadi()), dataStyle);
                
                // Data Pribadi Diproses
                createCell(row, colNum++, aplikasi.getDataPribadiDiproses(), wrapStyle);
                
                // Idle Info
                IdleInfo idleInfo = aplikasi.getIdleInfo();
                createCell(row, colNum++, idleInfo != null ? idleInfo.getKategoriIdle() : "", dataStyle);
                createCell(row, colNum++, idleInfo != null ? idleInfo.getAlasanIdle() : "", wrapStyle);
                createCell(row, colNum++, idleInfo != null ? idleInfo.getRencanaPengakhiran() : "", wrapStyle);
                createCell(row, colNum++, idleInfo != null ? idleInfo.getAlasanBelumDiakhiri() : "", wrapStyle);
                
                // URLs (multi-line)
                createCell(row, colNum++, formatUrls(aplikasi.getUrls()), wrapStyle);
                
                // Satker Internal (multi-line)
                createCell(row, colNum++, formatSatkerInternals(aplikasi.getSatkerInternals()), wrapStyle);
                
                // Pengguna Eksternal (multi-line)
                createCell(row, colNum++, formatPenggunaEksternals(aplikasi.getPenggunaEksternals()), wrapStyle);
                
                // Komunikasi Sistem (multi-line)
                createCell(row, colNum++, formatKomunikasiSistems(aplikasi.getKomunikasiSistems()), wrapStyle);
                
                // Penghargaan (multi-line)
                createCell(row, colNum++, formatPenghargaans(aplikasi.getPenghargaans()), wrapStyle);

                rowNum++;
            }

            // Auto-size columns with max width limit
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                // Limit max width to 50 characters (50 * 256)
                if (currentWidth > 12800) {
                    sheet.setColumnWidth(i, 12800);
                }
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            log.info("Successfully exported {} aplikasi records to Excel", aplikasiList.size());
            return outputStream;

        } catch (IOException e) {
            log.error("Error generating Excel file: {}", e.getMessage());
            throw new RuntimeException("Gagal membuat file Excel: " + e.getMessage(), e);
        }
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private CellStyle createWrapStyle(Workbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setWrapText(true);
        return style;
    }

    private void createCell(Row row, int colNum, Object value, CellStyle style) {
        Cell cell = row.createCell(colNum);
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else {
            cell.setCellValue(value != null ? value.toString() : "");
        }
        cell.setCellStyle(style);
    }

    private String formatDate(java.time.LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : "";
    }

    private String formatBoolean(Boolean value) {
        if (value == null) return "";
        return value ? "Ya" : "Tidak";
    }

    private String formatAkses(String akses) {
        if (akses == null || akses.isEmpty()) return "";
        // Replace commas with line breaks for better readability
        return akses.replace(",", LINE_BREAK).trim();
    }

    private String formatUrls(List<UrlInfo> urls) {
        if (urls == null || urls.isEmpty()) return "";
        return urls.stream()
                .map(u -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(u.getUrl() != null ? u.getUrl() : "");
                    if (u.getTipeAkses() != null && !u.getTipeAkses().isEmpty()) {
                        sb.append(" (").append(u.getTipeAkses()).append(")");
                    }
                    return sb.toString();
                })
                .collect(Collectors.joining(LINE_BREAK));
    }

    private String formatSatkerInternals(List<SatkerInternalInfo> satkers) {
        if (satkers == null || satkers.isEmpty()) return "";
        return satkers.stream()
                .map(s -> s.getNamaSatker() != null ? s.getNamaSatker() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(LINE_BREAK));
    }

    private String formatPenggunaEksternals(List<PenggunaEksternalInfo> pengguna) {
        if (pengguna == null || pengguna.isEmpty()) return "";
        return pengguna.stream()
                .map(p -> p.getNamaPengguna() != null ? p.getNamaPengguna() : "")
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(LINE_BREAK));
    }

    private String formatKomunikasiSistems(List<KomunikasiSistemInfo> komunikasi) {
        if (komunikasi == null || komunikasi.isEmpty()) return "";
        return komunikasi.stream()
                .map(k -> {
                    StringBuilder sb = new StringBuilder();
                    sb.append(k.getNamaSistem() != null ? k.getNamaSistem() : "");
                    if (k.getTipeSistem() != null && !k.getTipeSistem().isEmpty()) {
                        sb.append(" (").append(k.getTipeSistem()).append(")");
                    }
                    return sb.toString();
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(LINE_BREAK));
    }

    private String formatPenghargaans(List<PenghargaanInfo> penghargaans) {
        if (penghargaans == null || penghargaans.isEmpty()) return "";
        return penghargaans.stream()
                .map(p -> {
                    StringBuilder sb = new StringBuilder();
                    if (p.getKategori() != null && p.getKategori().getNama() != null) {
                        sb.append(p.getKategori().getNama());
                    }
                    if (p.getTanggal() != null) {
                        sb.append(" - ").append(formatDate(p.getTanggal()));
                    }
                    return sb.toString();
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(LINE_BREAK));
    }

    private String formatChangelogs(List<ChangelogInfo> changelogs) {
        if (changelogs == null || changelogs.isEmpty()) return "";
        return changelogs.stream()
                .map(c -> {
                    String tanggal = formatDate(c.getTanggalPerubahan());
                    String keterangan = c.getKeterangan() != null ? c.getKeterangan() : "";
                    if (!tanggal.isEmpty() && !keterangan.isEmpty()) {
                        return tanggal + ": " + keterangan;
                    } else if (!keterangan.isEmpty()) {
                        return keterangan;
                    } else {
                        return tanggal;
                    }
                })
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(LINE_BREAK));
    }

    @Override
    public ByteArrayOutputStream exportHistorisAplikasiToExcel(Integer tahun) {
        List<AplikasiSnapshotResponse> historisList = aplikasiHistorisService.getFullSnapshotsByTahun(tahun);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Historis Aplikasi Tahun " + tahun);

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle wrapStyle = createWrapStyle(workbook);

            // Define column headers - sama seperti export aplikasi + changelog
            String[] headers = {
                "No", "Kode Aplikasi", "Nama Aplikasi", "Deskripsi", "Status Aplikasi",
                "Tanggal Status", "Kode Bidang", "Nama Bidang", "Kode SKPA", "Nama SKPA",
                "Tanggal Implementasi", "Akses", "Proses Data Pribadi", "Data Pribadi Diproses",
                "Kategori Idle", "Alasan Idle", "Rencana Pengakhiran", "Alasan Belum Diakhiri",
                "URLs", "Satker Internal", "Pengguna Eksternal", "Komunikasi Sistem", "Penghargaan",
                "Changelog"
            };

            // Create header row
            Row headerRow = sheet.createRow(0);
            headerRow.setHeightInPoints(25);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Populate data rows
            int rowNum = 1;
            for (AplikasiSnapshotResponse snapshot : historisList) {
                Row row = sheet.createRow(rowNum);
                int colNum = 0;

                // No
                createCell(row, colNum++, rowNum, dataStyle);
                
                // Kode Aplikasi
                createCell(row, colNum++, snapshot.getKodeAplikasi(), dataStyle);
                
                // Nama Aplikasi
                createCell(row, colNum++, snapshot.getNamaAplikasi(), dataStyle);
                
                // Deskripsi
                createCell(row, colNum++, snapshot.getDeskripsi(), wrapStyle);
                
                // Status Aplikasi
                createCell(row, colNum++, snapshot.getStatusAplikasi(), dataStyle);
                
                // Tanggal Status
                createCell(row, colNum++, formatDate(snapshot.getTanggalStatus()), dataStyle);
                
                // Bidang
                BidangInfo bidang = snapshot.getBidang();
                createCell(row, colNum++, bidang != null ? bidang.getKodeBidang() : "", dataStyle);
                createCell(row, colNum++, bidang != null ? bidang.getNamaBidang() : "", dataStyle);
                
                // SKPA
                SkpaInfo skpa = snapshot.getSkpa();
                createCell(row, colNum++, skpa != null ? skpa.getKodeSkpa() : "", dataStyle);
                createCell(row, colNum++, skpa != null ? skpa.getNamaSkpa() : "", dataStyle);
                
                // Tanggal Implementasi
                createCell(row, colNum++, formatDate(snapshot.getTanggalImplementasi()), dataStyle);
                
                // Akses
                createCell(row, colNum++, formatAkses(snapshot.getAkses()), dataStyle);
                
                // Proses Data Pribadi
                createCell(row, colNum++, formatBoolean(snapshot.getProsesDataPribadi()), dataStyle);
                
                // Data Pribadi Diproses
                createCell(row, colNum++, snapshot.getDataPribadiDiproses(), wrapStyle);
                
                // Idle Info
                IdleInfo idleInfo = snapshot.getIdleInfo();
                createCell(row, colNum++, idleInfo != null ? idleInfo.getKategoriIdle() : "", dataStyle);
                createCell(row, colNum++, idleInfo != null ? idleInfo.getAlasanIdle() : "", wrapStyle);
                createCell(row, colNum++, idleInfo != null ? idleInfo.getRencanaPengakhiran() : "", wrapStyle);
                createCell(row, colNum++, idleInfo != null ? idleInfo.getAlasanBelumDiakhiri() : "", wrapStyle);
                
                // URLs (multi-line)
                createCell(row, colNum++, formatUrls(snapshot.getUrls()), wrapStyle);
                
                // Satker Internal (multi-line)
                createCell(row, colNum++, formatSatkerInternals(snapshot.getSatkerInternals()), wrapStyle);
                
                // Pengguna Eksternal (multi-line)
                createCell(row, colNum++, formatPenggunaEksternals(snapshot.getPenggunaEksternals()), wrapStyle);
                
                // Komunikasi Sistem (multi-line)
                createCell(row, colNum++, formatKomunikasiSistems(snapshot.getKomunikasiSistems()), wrapStyle);
                
                // Penghargaan (multi-line)
                createCell(row, colNum++, formatPenghargaans(snapshot.getPenghargaans()), wrapStyle);
                
                // Changelog (multi-line)
                createCell(row, colNum++, formatChangelogs(snapshot.getChangelogs()), wrapStyle);

                rowNum++;
            }

            // Auto-size columns with max width limit
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                int currentWidth = sheet.getColumnWidth(i);
                if (currentWidth > 12800) {
                    sheet.setColumnWidth(i, 12800);
                }
            }

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            log.info("Successfully exported {} historis aplikasi records for year {} to Excel", historisList.size(), tahun);
            return outputStream;

        } catch (IOException e) {
            log.error("Error generating Excel file for historis aplikasi: {}", e.getMessage());
            throw new RuntimeException("Gagal membuat file Excel: " + e.getMessage(), e);
        }
    }
}
