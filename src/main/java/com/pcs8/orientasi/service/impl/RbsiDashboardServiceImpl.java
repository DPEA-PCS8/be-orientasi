package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.RbsiDashboardRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse.DashboardSummary;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse.InisiatifPksiDetail;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse.KepProgressComparison;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse.PksiInfo;
import com.pcs8.orientasi.domain.dto.response.RbsiDashboardResponse.YearlyKepStatus;
import com.pcs8.orientasi.domain.entity.InisiatifGroup;
import com.pcs8.orientasi.domain.entity.KepProgress;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.Rbsi;
import com.pcs8.orientasi.domain.entity.RbsiInisiatif;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.InisiatifGroupRepository;
import com.pcs8.orientasi.repository.KepProgressRepository;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.repository.RbsiRepository;
import com.pcs8.orientasi.util.NomorComparator;
import com.pcs8.orientasi.service.RbsiDashboardService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbsiDashboardServiceImpl implements RbsiDashboardService {

    private static final Logger log = LoggerFactory.getLogger(RbsiDashboardServiceImpl.class);
    private static final String DISCREPANCY_SHOULD_HAVE = "SHOULD_HAVE_PKSI";
    private static final String DISCREPANCY_UNEXPECTED = "UNEXPECTED_PKSI";

    private final RbsiRepository rbsiRepository;
    private final InisiatifGroupRepository inisiatifGroupRepository;
    private final PksiDocumentRepository pksiDocumentRepository;
    private final KepProgressRepository kepProgressRepository;

    @Override
    @Transactional(readOnly = true)
    public RbsiDashboardResponse getDashboardData(UUID rbsiId, RbsiDashboardRequest request) {
        log.info("Getting dashboard data for RBSI: {}, tahun: {}", rbsiId, request.getTahun());

        // Get RBSI
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        // Get available years from KEP progress based on kepId filter
        List<Integer> availableYears = request.getKepId() != null ?
                kepProgressRepository.findDistinctTahunByRbsiIdAndKepId(rbsiId, request.getKepId()) :
                kepProgressRepository.findDistinctTahunByRbsiId(rbsiId);

        Integer selectedTahun = request.getTahun() != null ? request.getTahun() : 
                (availableYears.isEmpty() ? LocalDate.now().getYear() : availableYears.get(availableYears.size() - 1));

        Integer comparisonTahun = request.getComparisonYear() != null ? request.getComparisonYear() : selectedTahun - 1;

        // Get all initiative groups for this RBSI
        List<InisiatifGroup> groups = inisiatifGroupRepository.findByRbsiIdOrderByCreatedAtAsc(rbsiId);
        List<UUID> groupIds = groups.stream().map(InisiatifGroup::getId).collect(Collectors.toList());

        // Get all inisiatifs for selected year
        List<RbsiInisiatif> inisiatifs = getInisiatifsByYearFromGroups(groups, selectedTahun);

        // Get PKSI documents linked to these initiative groups
        String pksiStatusFilter = request.getPksiStatus();
        List<PksiDocument> pksiDocuments = groupIds.isEmpty() ? new ArrayList<>() :
                (pksiStatusFilter == null || pksiStatusFilter.isEmpty() ?
                        pksiDocumentRepository.findByInisiatifGroupIdIn(groupIds) :
                        pksiDocumentRepository.findByInisiatifGroupIdInAndStatus(groupIds, pksiStatusFilter));

        // Group PKSI by initiative group
        Map<UUID, List<PksiDocument>> pksiByGroup = pksiDocuments.stream()
                .filter(p -> p.getInisiatifGroup() != null)
                .collect(Collectors.groupingBy(p -> p.getInisiatifGroup().getId()));

        // Get KEP progress for comparison (filter by kepId if provided)
        List<KepProgress> allKepProgress = request.getKepId() != null ?
                kepProgressRepository.findByRbsiIdAndKepId(rbsiId, request.getKepId()) :
                kepProgressRepository.findAllByRbsiId(rbsiId);
        Map<UUID, Map<Integer, KepProgress>> kepProgressByGroupAndYear = buildKepProgressMap(allKepProgress);

        // Build initiative details
        List<InisiatifPksiDetail> initiativeDetails = buildInitiativeDetails(
                inisiatifs, groups, pksiByGroup, kepProgressByGroupAndYear, selectedTahun, comparisonTahun, availableYears
        );

        // Calculate summary stats
        int totalInisiatif = initiativeDetails.size();
        int withPksi = (int) initiativeDetails.stream().filter(InisiatifPksiDetail::getHasPksi).count();
        int withoutPksi = totalInisiatif - withPksi;
        double percentage = totalInisiatif > 0 ? (withPksi * 100.0 / totalInisiatif) : 0;

        // Calculate KEP expected vs actual for selected year
        int kepExpectedWithPksi = 0;
        int kepRealizedWithPksi = 0;
        int kepMissingPksi = 0;
        int kepUnexpectedPksi = 0;

        for (InisiatifPksiDetail detail : initiativeDetails) {
            KepProgressComparison comparison = detail.getKepProgressComparison();
            if (comparison != null && comparison.getYearlyStatus() != null) {
                YearlyKepStatus yearStatus = comparison.getYearlyStatus().get(selectedTahun);
                if (yearStatus != null) {
                    boolean kepExpects = !"none".equalsIgnoreCase(yearStatus.getKepStatus());
                    boolean actuallyHasPksi = Boolean.TRUE.equals(yearStatus.getHasPksiInYear());
                    
                    if (kepExpects) {
                        kepExpectedWithPksi++;
                        if (actuallyHasPksi) {
                            kepRealizedWithPksi++;
                        } else {
                            kepMissingPksi++;
                        }
                    } else if (actuallyHasPksi) {
                        kepUnexpectedPksi++;
                    }
                }
            }
        }

        double kepCompliance = kepExpectedWithPksi > 0 ? (kepRealizedWithPksi * 100.0 / kepExpectedWithPksi) : 100.0;

        DashboardSummary summary = DashboardSummary.builder()
                .totalInisiatif(totalInisiatif)
                .inisiatifWithPksi(withPksi)
                .inisiatifWithoutPksi(withoutPksi)
                .percentageWithPksi(Math.round(percentage * 100.0) / 100.0)
                .kepExpectedWithPksi(kepExpectedWithPksi)
                .kepRealizedWithPksi(kepRealizedWithPksi)
                .kepMissingPksi(kepMissingPksi)
                .kepUnexpectedPksi(kepUnexpectedPksi)
                .kepCompliancePercentage(Math.round(kepCompliance * 100.0) / 100.0)
                .build();

        return RbsiDashboardResponse.builder()
                .rbsiId(rbsiId)
                .periode(rbsi.getPeriode())
                .selectedTahun(selectedTahun)
                .comparisonTahun(comparisonTahun)
                .summary(summary)
                .initiatives(initiativeDetails)
                .availableYears(availableYears)
                .build();
    }

    private List<RbsiInisiatif> getInisiatifsByYearFromGroups(List<InisiatifGroup> groups, Integer tahun) {
        List<RbsiInisiatif> result = new ArrayList<>();
        for (InisiatifGroup group : groups) {
            if (group.getInisiatifs() != null) {
                for (RbsiInisiatif inisiatif : group.getInisiatifs()) {
                    if (tahun.equals(inisiatif.getTahun()) && !Boolean.TRUE.equals(inisiatif.getIsDeleted())) {
                        result.add(inisiatif);
                    }
                }
            }
        }
        return result;
    }

    private Map<UUID, Map<Integer, KepProgress>> buildKepProgressMap(List<KepProgress> allKepProgress) {
        Map<UUID, Map<Integer, KepProgress>> result = new HashMap<>();
        for (KepProgress kp : allKepProgress) {
            UUID groupId = kp.getInisiatifGroup().getId();
            result.computeIfAbsent(groupId, k -> new HashMap<>())
                    .put(kp.getTahun(), kp);
        }
        return result;
    }

    private List<InisiatifPksiDetail> buildInitiativeDetails(
            List<RbsiInisiatif> inisiatifs,
            List<InisiatifGroup> groups,
            Map<UUID, List<PksiDocument>> pksiByGroup,
            Map<UUID, Map<Integer, KepProgress>> kepProgressByGroupAndYear,
            Integer selectedTahun,
            Integer comparisonTahun,
            List<Integer> availableYears
    ) {
        List<InisiatifPksiDetail> details = new ArrayList<>();

        for (RbsiInisiatif inisiatif : inisiatifs) {
            UUID groupId = inisiatif.getGroup().getId();
            
            List<PksiDocument> groupPksi = pksiByGroup.getOrDefault(groupId, new ArrayList<>());
            // boolean hasPksi = !groupPksi.isEmpty();

            // Check if any PKSI covers the selected year (for multiyear)
            boolean hasPksiInSelectedYear = hasPksiInYear(groupPksi, selectedTahun);

            // Build PKSI info list - only include PKSI that covers the selected year
            List<PksiInfo> pksiList = groupPksi.stream()
                    .filter(pksi -> isPksiCoveringYear(pksi, selectedTahun))
                    .map(this::mapToPksiInfo)
                    .collect(Collectors.toList());

            // Build KEP progress comparison
            KepProgressComparison kepComparison = buildKepProgressComparison(
                    groupId, kepProgressByGroupAndYear, selectedTahun, comparisonTahun, 
                    availableYears, hasPksiInSelectedYear, groupPksi
            );

            // Get program name from inisiatif
            String programNama = inisiatif.getProgram() != null ? 
                    inisiatif.getProgram().getNamaProgram() : "";

            details.add(InisiatifPksiDetail.builder()
                    .groupId(groupId)
                    .namaInisiatif(inisiatif.getNamaInisiatif())
                    .nomorInisiatif(inisiatif.getNomorInisiatif())
                    .programNama(programNama)
                    .hasPksi(hasPksiInSelectedYear)
                    .pksiList(pksiList)
                    .kepProgressComparison(kepComparison)
                    .build());
        }

        // Sort by nomor inisiatif with numeric-aware comparison
        details.sort((a, b) -> {
            String nomorA = a.getNomorInisiatif();
            String nomorB = b.getNomorInisiatif();
            return NomorComparator.compare(nomorA, nomorB);
        });

        return details;
    }

    private boolean hasPksiInYear(List<PksiDocument> pksiList, Integer year) {
        return pksiList.stream().anyMatch(pksi -> isPksiCoveringYear(pksi, year));
    }

    private boolean isPksiCoveringYear(PksiDocument pksi, Integer year) {
        // Get start and end years from PKSI
        Integer startYear = pksi.getTargetUsreq() != null ? pksi.getTargetUsreq().getYear() : null;
        Integer endYear = pksi.getTargetGoLive() != null ? pksi.getTargetGoLive().getYear() : null;

        if (startYear == null && endYear == null) {
            // If no dates, use tanggalPengajuan year
            return pksi.getTanggalPengajuan() != null && 
                    pksi.getTanggalPengajuan().getYear() == year;
        }

        if (startYear == null) startYear = year;
        if (endYear == null) endYear = year;

        return year >= startYear && year <= endYear;
    }

    private PksiInfo mapToPksiInfo(PksiDocument pksi) {
        Integer startYear = pksi.getTargetUsreq() != null ? pksi.getTargetUsreq().getYear() : null;
        Integer endYear = pksi.getTargetGoLive() != null ? pksi.getTargetGoLive().getYear() : null;
        boolean isMultiyear = startYear != null && endYear != null && !startYear.equals(endYear);

        return PksiInfo.builder()
                .id(pksi.getId())
                .namaPksi(pksi.getNamaPksi())
                .status(pksi.getStatus() != null ? pksi.getStatus().name() : null)
                .tahunPelaksanaanAwal(startYear)
                .tahunPelaksanaanAkhir(endYear)
                .isMultiyear(isMultiyear)
                .build();
    }

    private KepProgressComparison buildKepProgressComparison(
            UUID groupId,
            Map<UUID, Map<Integer, KepProgress>> kepProgressByGroupAndYear,
            Integer selectedTahun,
            Integer comparisonTahun,
            List<Integer> availableYears,
            boolean hasPksiInSelectedYear,
            List<PksiDocument> groupPksi
    ) {
        Map<Integer, YearlyKepStatus> yearlyStatus = new HashMap<>();
        Map<Integer, KepProgress> groupProgress = kepProgressByGroupAndYear.getOrDefault(groupId, new HashMap<>());

        // Build status for each available year
        for (Integer year : availableYears) {
            KepProgress kp = groupProgress.get(year);
            String kepStatus = kp != null ? kp.getStatus().name() : "none";
            boolean hasPksiInYear = hasPksiInYear(groupPksi, year);
            boolean isSelectedYear = year.equals(selectedTahun);

            // Detect discrepancies
            String discrepancyType = null;
            String discrepancyMessage = null;
            boolean isHighlighted = false;

            if (isSelectedYear) {
                // Only check discrepancy for selected year
                boolean kepExpectsPksi = !"none".equalsIgnoreCase(kepStatus);

                if (kepExpectsPksi && !hasPksiInYear) {
                    discrepancyType = DISCREPANCY_SHOULD_HAVE;
                    discrepancyMessage = "Seharusnya tahun ini inisiatif ini memiliki PKSI, tapi ternyata belum.";
                    isHighlighted = true;
                } else if (!kepExpectsPksi && hasPksiInYear) {
                    discrepancyType = DISCREPANCY_UNEXPECTED;
                    discrepancyMessage = "Seharusnya tahun ini inisiatif ini belum memiliki PKSI, tapi ternyata sudah.";
                    isHighlighted = true;
                }
            }

            yearlyStatus.put(year, YearlyKepStatus.builder()
                    .tahun(year)
                    .kepStatus(kepStatus)
                    .hasPksiInYear(hasPksiInYear)
                    .discrepancyType(discrepancyType)
                    .discrepancyMessage(discrepancyMessage)
                    .isHighlighted(isHighlighted || isSelectedYear)
                    .build());
        }

        return KepProgressComparison.builder()
                .yearlyStatus(yearlyStatus)
                .build();
    }
}
