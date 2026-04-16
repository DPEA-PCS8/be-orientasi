package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.FormasiEfektifRequest;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifDetailResponse;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifResponse;

import java.util.List;

/**
 * Service for Formasi Efektif calculations and management
 */
public interface FormasiEfektifService {

    /**
     * Get dashboard data with summary calculations
     *
     * @param request Request with year filter
     * @return Dashboard response with summaries
     */
    FormasiEfektifResponse getDashboardData(FormasiEfektifRequest request);

    /**
     * Get detail data with calculation breakdowns
     *
     * @param request Request with year filter
     * @return Detail response with PKSI/FS2 breakdowns
     */
    FormasiEfektifDetailResponse getDetailData(FormasiEfektifRequest request);

    /**
     * Update configuration parameters
     *
     * @param parameters List of parameters to update
     * @return Updated parameters
     */
    List<FormasiEfektifResponse.ParameterItem> updateParameters(List<FormasiEfektifResponse.ParameterItem> parameters);

    /**
     * Get all configuration parameters for FORMASI_EFEKTIF
     *
     * @return List of parameters
     */
    List<FormasiEfektifResponse.ParameterItem> getParameters();
}
