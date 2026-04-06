package com.pcs8.orientasi.service;

import java.io.ByteArrayOutputStream;

/**
 * Service for exporting Aplikasi data to Excel format
 */
public interface AplikasiExcelExportService {

    /**
     * Export all aplikasi data to Excel format
     * @return ByteArrayOutputStream containing the Excel file
     */
    ByteArrayOutputStream exportAplikasiToExcel();

    /**
     * Export historis aplikasi data for a specific year to Excel format
     * @param tahun The year to export
     * @return ByteArrayOutputStream containing the Excel file
     */
    ByteArrayOutputStream exportHistorisAplikasiToExcel(Integer tahun);
}
