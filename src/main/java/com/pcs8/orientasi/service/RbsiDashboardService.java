package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.RbsiDashboardRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse;

import java.util.UUID;

/**
 * Service for RBSI Dashboard analytics - provides insights on initiative-PKSI relationships
 */
public interface RbsiDashboardService {

    /**
     * Get dashboard data for RBSI showing initiative-PKSI relationships
     * 
     * @param rbsiId The RBSI ID
     * @param request Dashboard request with filters
     * @return Dashboard response with summary and detailed initiative data
     */
    RbsiDashboardResponse getDashboardData(UUID rbsiId, RbsiDashboardRequest request);
}
