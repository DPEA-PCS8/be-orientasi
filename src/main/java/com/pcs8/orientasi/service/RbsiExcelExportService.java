package com.pcs8.orientasi.service;

import java.io.ByteArrayOutputStream;
import java.util.UUID;

public interface RbsiExcelExportService {
    
    /**
     * Export RBSI monitoring data to Excel format
     * @param rbsiId the RBSI ID
     * @return ByteArrayOutputStream containing the Excel file
     */
    ByteArrayOutputStream exportMonitoringToExcel(UUID rbsiId);
}
