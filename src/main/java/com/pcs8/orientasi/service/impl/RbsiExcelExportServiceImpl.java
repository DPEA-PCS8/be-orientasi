package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.response.RbsiMonitoringResponse;
import com.pcs8.orientasi.service.RbsiExcelExportService;
import com.pcs8.orientasi.service.RbsiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class RbsiExcelExportServiceImpl implements RbsiExcelExportService {

    private final RbsiService rbsiService;

    @Override
    public ByteArrayOutputStream exportMonitoringToExcel(UUID rbsiId) {
        RbsiMonitoringResponse monitoringData = rbsiService.getMonitoringData(rbsiId);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Monitoring Progress RBSI");

            // Get KEP list and period years
            List<RbsiMonitoringResponse.KepInfo> kepList = monitoringData.getKepList();
            if (kepList.isEmpty()) {
                throw new IllegalStateException("Tidak ada data KEP untuk di-export");
            }

            // Determine period years from KEP data
            int minYear = kepList.stream().mapToInt(RbsiMonitoringResponse.KepInfo::getTahunPelaporan).min().orElse(2024);
            int maxYear = kepList.stream().mapToInt(RbsiMonitoringResponse.KepInfo::getTahunPelaporan).max().orElse(2027);
            List<Integer> periodYears = new ArrayList<>();
            for (int year = minYear; year <= maxYear; year++) {
                periodYears.add(year);
            }
            int yearCount = periodYears.size();
            int kepCount = kepList.size();

            // Create styles
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle progressHeaderStyle = createProgressHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle yearHeaderStyle = createYearHeaderStyle(workbook);
            CellStyle programStyle = createProgramStyle(workbook);
            CellStyle programCenterStyle = createProgramCenterStyle(workbook);
            CellStyle initiativeStyle = createInitiativeStyle(workbook);
            CellStyle initiativeNewStyle = createInitiativeNewStyle(workbook);
            CellStyle initiativeModStyle = createInitiativeModStyle(workbook);
            CellStyle initiativeDelStyle = createInitiativeDelStyle(workbook);
            CellStyle checkStyle = createCheckStyle(workbook);
            CellStyle noDataStyle = createNoDataStyle(workbook);

            int rowNum = 0;

            // Column layout: 
            // [KEP1 Name] [KEP2 Name] [KEP3 Name] | [KEP1 Progress Years] [KEP2 Progress Years] [KEP3 Progress Years]
            // Total columns = kepCount (names) + kepCount * yearCount (progress)

            // ==================== ROW 0: KEP Headers ====================
            Row kepHeaderRow = sheet.createRow(rowNum++);
            kepHeaderRow.setHeightInPoints(25);
            
            // First: All KEP name headers
            for (int k = 0; k < kepCount; k++) {
                Cell nameCell = kepHeaderRow.createCell(k);
                nameCell.setCellValue(kepList.get(k).getNomorKep());
                nameCell.setCellStyle(headerStyle);
            }
            
            // Then: All KEP progress headers (merged across year columns)
            int progressStartCol = kepCount;
            for (int k = 0; k < kepCount; k++) {
                int startCol = progressStartCol + (k * yearCount);
                int endCol = startCol + yearCount - 1;
                
                Cell progressCell = kepHeaderRow.createCell(startCol);
                progressCell.setCellValue("Progress " + kepList.get(k).getNomorKep());
                progressCell.setCellStyle(progressHeaderStyle);
                
                // Apply style to all merged cells
                for (int c = startCol + 1; c <= endCol; c++) {
                    Cell cell = kepHeaderRow.createCell(c);
                    cell.setCellStyle(progressHeaderStyle);
                }
                
                // Merge progress header cells
                if (yearCount > 1) {
                    sheet.addMergedRegion(new CellRangeAddress(0, 0, startCol, endCol));
                }
            }

            // ==================== ROW 1: Sub-headers ====================
            Row subHeaderRow = sheet.createRow(rowNum++);
            subHeaderRow.setHeightInPoints(20);
            
            // First: All "Program/Inisiatif" sub-headers
            for (int k = 0; k < kepCount; k++) {
                Cell nameSubCell = subHeaderRow.createCell(k);
                nameSubCell.setCellValue("Program/Inisiatif");
                nameSubCell.setCellStyle(subHeaderStyle);
            }
            
            // Then: All year sub-headers for each KEP
            for (int k = 0; k < kepCount; k++) {
                int startCol = kepCount + (k * yearCount);
                for (int y = 0; y < yearCount; y++) {
                    Cell yearCell = subHeaderRow.createCell(startCol + y);
                    yearCell.setCellValue(String.valueOf(periodYears.get(y)).substring(2)); // Last 2 digits
                    yearCell.setCellStyle(yearHeaderStyle);
                }
            }

            // ==================== DATA ROWS ====================
            List<RbsiMonitoringResponse.ProgramMonitoring> programs = monitoringData.getPrograms();
            
            for (RbsiMonitoringResponse.ProgramMonitoring program : programs) {
                // --- Program Row ---
                Row programRow = sheet.createRow(rowNum++);
                
                // First: All KEP name columns for program
                for (int k = 0; k < kepCount; k++) {
                    RbsiMonitoringResponse.KepInfo kep = kepList.get(k);
                    RbsiMonitoringResponse.ProgramVersion programVersion = 
                        program.getVersionsByYear().get(kep.getTahunPelaporan());
                    
                    Cell progNameCell = programRow.createCell(k);
                    if (programVersion != null) {
                        progNameCell.setCellValue(program.getNomorProgram() + " - " + programVersion.getNamaProgram());
                        progNameCell.setCellStyle(programStyle);
                    } else {
                        progNameCell.setCellValue("—");
                        progNameCell.setCellStyle(noDataStyle);
                    }
                }
                
                // Then: All KEP progress columns for program
                for (int k = 0; k < kepCount; k++) {
                    RbsiMonitoringResponse.KepInfo kep = kepList.get(k);
                    RbsiMonitoringResponse.ProgramVersion programVersion = 
                        program.getVersionsByYear().get(kep.getTahunPelaporan());
                    
                    int startCol = kepCount + (k * yearCount);
                    
                    if (programVersion != null) {
                        long iniCount = program.getInitiatives().stream()
                            .filter(ini -> ini.getVersionsByYear().containsKey(kep.getTahunPelaporan()))
                            .count();
                        
                        Cell countCell = programRow.createCell(startCol);
                        countCell.setCellValue(iniCount + " inisiatif");
                        countCell.setCellStyle(programCenterStyle);
                        
                        // Merge across all year columns for program row
                        if (yearCount > 1) {
                            sheet.addMergedRegion(new CellRangeAddress(
                                programRow.getRowNum(), programRow.getRowNum(), 
                                startCol, startCol + yearCount - 1
                            ));
                        }
                        
                        // Apply style to merged cells
                        for (int y = 1; y < yearCount; y++) {
                            Cell cell = programRow.createCell(startCol + y);
                            cell.setCellStyle(programCenterStyle);
                        }
                    } else {
                        // Empty cells for no data
                        for (int y = 0; y < yearCount; y++) {
                            Cell cell = programRow.createCell(startCol + y);
                            cell.setCellStyle(programCenterStyle);
                        }
                    }
                }

                // --- Initiative Rows ---
                for (RbsiMonitoringResponse.InitiativeMonitoring initiative : program.getInitiatives()) {
                    Row iniRow = sheet.createRow(rowNum++);
                    
                    // First: All KEP name columns for initiative
                    for (int k = 0; k < kepCount; k++) {
                        RbsiMonitoringResponse.KepInfo kep = kepList.get(k);
                        RbsiMonitoringResponse.InitiativeVersion iniVersion = 
                            initiative.getVersionsByYear().get(kep.getTahunPelaporan());
                        
                        Cell iniNameCell = iniRow.createCell(k);
                        if (iniVersion != null) {
                            iniNameCell.setCellValue("    " + iniVersion.getNomorInisiatif() + " - " + 
                                iniVersion.getNamaInisiatif());
                            
                            // Apply colored style based on status badge
                            if ("new".equals(iniVersion.getStatusBadge())) {
                                iniNameCell.setCellStyle(initiativeNewStyle);
                            } else if ("modified".equals(iniVersion.getStatusBadge())) {
                                iniNameCell.setCellStyle(initiativeModStyle);
                            } else if ("deleted".equals(iniVersion.getStatusBadge())) {
                                iniNameCell.setCellStyle(initiativeDelStyle);
                            } else {
                                iniNameCell.setCellStyle(initiativeStyle);
                            }
                        } else {
                            iniNameCell.setCellValue("    —");
                            iniNameCell.setCellStyle(noDataStyle);
                        }
                    }
                    
                    // Then: All KEP progress columns for initiative
                    for (int k = 0; k < kepCount; k++) {
                        RbsiMonitoringResponse.KepInfo kep = kepList.get(k);
                        RbsiMonitoringResponse.InitiativeVersion iniVersion = 
                            initiative.getVersionsByYear().get(kep.getTahunPelaporan());
                        RbsiMonitoringResponse.KepProgress kepProgress = 
                            initiative.getProgressByKep().get(kep.getId());
                        
                        int startCol = kepCount + (k * yearCount);
                        
                        for (int y = 0; y < yearCount; y++) {
                            Cell yearCell = iniRow.createCell(startCol + y);
                            
                            if (iniVersion != null) {
                                int targetYear = periodYears.get(y);
                                String status = "none";
                                
                                if (kepProgress != null && kepProgress.getYearlyProgress() != null) {
                                    Optional<RbsiMonitoringResponse.YearlyProgress> yearProgress = 
                                        kepProgress.getYearlyProgress().stream()
                                            .filter(yp -> yp.getTahun().equals(targetYear))
                                            .findFirst();
                                    if (yearProgress.isPresent()) {
                                        status = yearProgress.get().getStatus();
                                    }
                                }
                                
                                switch (status) {
                                    case "realized":
                                        yearCell.setCellValue("✓");
                                        break;
                                    case "planned":
                                        yearCell.setCellValue("○");
                                        break;
                                    default:
                                        yearCell.setCellValue("·");
                                        break;
                                }
                            } else {
                                yearCell.setCellValue("");
                            }
                            yearCell.setCellStyle(checkStyle);
                        }
                    }
                }
            }

            // ==================== COLUMN SIZING ====================
            // Name columns - wider
            for (int k = 0; k < kepCount; k++) {
                sheet.setColumnWidth(k, 45 * 256); // ~45 characters
            }
            
            // Year columns - narrower
            for (int k = 0; k < kepCount; k++) {
                int startCol = kepCount + (k * yearCount);
                for (int y = 0; y < yearCount; y++) {
                    sheet.setColumnWidth(startCol + y, 5 * 256); // ~5 characters
                }
            }

            // ==================== LEGEND ====================
            rowNum += 2;
            Row legendRow = sheet.createRow(rowNum);
            Cell legendCell = legendRow.createCell(0);
            legendCell.setCellValue("Keterangan: ✓ = Terealisasi | ○ = Direncanakan | · = Tidak ada");
            
            // Color legend
            Row colorLegendRow = sheet.createRow(rowNum + 1);
            Cell colorLegendCell = colorLegendRow.createCell(0);
            colorLegendCell.setCellValue("Warna: Hijau = Baru | Kuning = Diubah | Merah = Dihapus");

            // Write to output stream
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream;

        } catch (IOException e) {
            log.error("Failed to generate Excel file", e);
            throw new RuntimeException("Gagal membuat file Excel: " + e.getMessage());
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

    private CellStyle createProgressHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
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

    private CellStyle createYearHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createProgramStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createProgramCenterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createInitiativeStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createInitiativeNewStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createInitiativeModStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createInitiativeDelStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.CORAL.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setWrapText(true);
        return style;
    }

    private CellStyle createCheckStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createNoDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 9);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        font.setItalic(true);
        style.setFont(font);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }
}
