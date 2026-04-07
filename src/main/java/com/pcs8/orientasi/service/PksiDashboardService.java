package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.PksiDashboardRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDashboardResponse;

/**
 * Service for PKSI Dashboard analytics - provides insights on PKSI data
 */
public interface PksiDashboardService {

    /**
     * Get dashboard data for PKSI showing analytics and insights
     * 
     * @param request Dashboard request with filters (tahun, bulan, status)
     * @return Dashboard response with summary and detailed data
     */
    PksiDashboardResponse getDashboardData(PksiDashboardRequest request);
}
