package com.pcs8.orientasi.service;

import java.io.ByteArrayOutputStream;
import java.util.Set;
import java.util.UUID;

public interface Fs2ExcelExportService {
    
    /**
     * Export all F.S.2 documents to Excel format with optional filters
     * @param search search term
     * @param aplikasiId filter by application ID
     * @param statusTahapan filter by tahapan status
     * @param skpaId filter by SKPA
     * @param status filter by status
     * @param year filter by year
     * @param startMonth filter by start month
     * @param endMonth filter by end month
     * @param userDepartment filter by user's department
     * @param canSeeAll whether user can see all records
     * @return ByteArrayOutputStream containing the Excel file
     */
    ByteArrayOutputStream exportAllFs2ToExcel(
            String search, UUID aplikasiId, String statusTahapan, UUID skpaId, String status,
            Integer year, Integer startMonth, Integer endMonth,
            String userDepartment, boolean canSeeAll);
    
    /**
     * Export approved F.S.2 documents (Monitoring) to Excel format with optional filters
     * @param search search term
     * @param bidangId filter by bidang
     * @param skpaId filter by SKPA
     * @param progres filter by progress
     * @param fasePengajuan filter by fase pengajuan
     * @param mekanisme filter by mekanisme
     * @param pelaksanaan filter by pelaksanaan
     * @param year filter by year
     * @param startMonth filter by start month
     * @param endMonth filter by end month
     * @param userDepartment user's department for filtering
     * @param canSeeAll whether user can see all data
     * @return ByteArrayOutputStream containing the Excel file
     */
    ByteArrayOutputStream exportApprovedFs2ToExcel(
            String search, UUID bidangId, UUID skpaId, String progres,
            String fasePengajuan, String mekanisme, String pelaksanaan,
            Integer year, Integer startMonth, Integer endMonth,
            String userDepartment, boolean canSeeAll);
}
