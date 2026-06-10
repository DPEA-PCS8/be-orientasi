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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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

        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        // All initiative groups for this RBSI
        List<InisiatifGroup> groups = inisiatifGroupRepository.findByRbsiIdAndIsDeletedFalseOrderByCreatedAtAsc(rbsiId);
        List<UUID> groupIds = groups.stream().map(InisiatifGroup::getId).collect(Collectors.toList());

        // Available years = from KEP progress (drives the year selector)
        List<Integer> availableYears = request.getKepId() != null ?
                kepProgressRepository.findDistinctTahunByRbsiIdAndKepId(rbsiId, request.getKepId()) :
                kepProgressRepository.findDistinctTahunByRbsiId(rbsiId);

        Integer selectedTahun = request.getTahun() != null ? request.getTahun() :
                (availableYears.isEmpty() ? LocalDate.now().getYear() : availableYears.get(availableYears.size() - 1));
        Integer comparisonTahun = request.getComparisonYear() != null ? request.getComparisonYear() : selectedTahun - 1;

        // PKSIs in selectedTahun: filtered by timeline year (includes PKSIs with no inisiatif group)
        String pksiStatusFilter = request.getPksiStatus();
        List<PksiDocument> pksiDocuments = groupIds.isEmpty() ? new ArrayList<>() :
                pksiDocumentRepository.findByGroupIdsOrNullGroupAndTimelineYear(
                        groupIds, selectedTahun, pksiStatusFilter);

        // Build pksiId → set<year> map for ALL timeline years of these PKSIs (for year-range display)
        List<UUID> pksiIds = pksiDocuments.stream().map(PksiDocument::getId).collect(Collectors.toList());
        Map<UUID, Set<Integer>> pksiTimelineYears = pksiIds.isEmpty() ?
                new HashMap<>() : buildPksiTimelineYearsMapByPksiIds(pksiIds);

        // Group PKSI by initiative group
        Map<UUID, List<PksiDocument>> pksiByGroup = pksiDocuments.stream()
                .filter(p -> p.getInisiatifGroup() != null)
                .collect(Collectors.groupingBy(p -> p.getInisiatifGroup().getId()));

        // KEP progress for comparison (filter by kepId if provided)
        List<KepProgress> allKepProgress = request.getKepId() != null ?
                kepProgressRepository.findByRbsiIdAndKepId(rbsiId, request.getKepId()) :
                kepProgressRepository.findAllByRbsiId(rbsiId);
        Map<UUID, Map<Integer, KepProgress>> kepProgressByGroupAndYear = buildKepProgressMap(allKepProgress);

        // Build initiative details — iterate ALL groups (program/inisiatif from KEP context)
        List<InisiatifPksiDetail> initiativeDetails = buildInitiativeDetails(
                groups, pksiByGroup, pksiTimelineYears, kepProgressByGroupAndYear,
                selectedTahun, comparisonTahun, availableYears
        );

        // Summary stats
        int totalInisiatif = initiativeDetails.size();
        int withPksi = (int) initiativeDetails.stream().filter(InisiatifPksiDetail::getHasPksi).count();
        int withoutPksi = totalInisiatif - withPksi;
        double percentage = totalInisiatif > 0 ? (withPksi * 100.0 / totalInisiatif) : 0;

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
                        if (actuallyHasPksi) kepRealizedWithPksi++;
                        else kepMissingPksi++;
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

    /**
     * Build pksiId → set-of-years map from pksi IDs (all timeline years for those PKSIs).
     * Used to show full year range (isMultiyear, tahunAwal/Akhir) after PKSIs are already fetched.
     */
    private Map<UUID, Set<Integer>> buildPksiTimelineYearsMapByPksiIds(List<UUID> pksiIds) {
        Map<UUID, Set<Integer>> result = new HashMap<>();
        List<Object[]> rows = pksiDocumentRepository.findPksiIdAndTimelineYearsByPksiIds(pksiIds);
        for (Object[] row : rows) {
            UUID pksiId = (UUID) row[0];
            Integer year = ((Number) row[1]).intValue();
            result.computeIfAbsent(pksiId, k -> new HashSet<>()).add(year);
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

    /**
     * Iterate over ALL groups (not year-filtered RbsiInisiatif instances).
     * Program/inisiatif display info: pick the year-specific RbsiInisiatif if available,
     * otherwise fall back to the group's latest non-deleted instance.
     */
    private List<InisiatifPksiDetail> buildInitiativeDetails(
            List<InisiatifGroup> groups,
            Map<UUID, List<PksiDocument>> pksiByGroup,
            Map<UUID, Set<Integer>> pksiTimelineYears,
            Map<UUID, Map<Integer, KepProgress>> kepProgressByGroupAndYear,
            Integer selectedTahun,
            Integer comparisonTahun,
            List<Integer> availableYears
    ) {
        List<InisiatifPksiDetail> details = new ArrayList<>();

        for (InisiatifGroup group : groups) {
            UUID groupId = group.getId();
            List<PksiDocument> groupPksi = pksiByGroup.getOrDefault(groupId, new ArrayList<>());

            boolean hasPksiInSelectedYear = hasPksiInYear(groupPksi, selectedTahun, pksiTimelineYears);

            List<PksiInfo> pksiList = groupPksi.stream()
                    .filter(pksi -> isPksiCoveringYear(pksi, selectedTahun, pksiTimelineYears))
                    .map(pksi -> mapToPksiInfo(pksi, pksiTimelineYears))
                    .collect(Collectors.toList());

            KepProgressComparison kepComparison = buildKepProgressComparison(
                    groupId, kepProgressByGroupAndYear, selectedTahun, comparisonTahun,
                    availableYears, hasPksiInSelectedYear, groupPksi, pksiTimelineYears
            );

            // Pick representative RbsiInisiatif for display (prefer selectedTahun, else latest)
            RbsiInisiatif rep = getRepresentativeInisiatif(group, selectedTahun);
            String namaInisiatif = rep != null ? rep.getNamaInisiatif() : group.getNamaInisiatif();
            String nomorInisiatif = rep != null ? rep.getNomorInisiatif() : "";
            String programNama = (rep != null && rep.getProgram() != null) ? rep.getProgram().getNamaProgram() : "";

            details.add(InisiatifPksiDetail.builder()
                    .groupId(groupId)
                    .namaInisiatif(namaInisiatif)
                    .nomorInisiatif(nomorInisiatif)
                    .programNama(programNama)
                    .hasPksi(hasPksiInSelectedYear)
                    .pksiList(pksiList)
                    .kepProgressComparison(kepComparison)
                    .build());
        }

        details.sort((a, b) -> NomorComparator.compare(a.getNomorInisiatif(), b.getNomorInisiatif()));

        return details;
    }

    /**
     * Pick the best RbsiInisiatif to represent a group for display purposes.
     * Prefer the instance matching selectedTahun; fall back to latest non-deleted.
     */
    private RbsiInisiatif getRepresentativeInisiatif(InisiatifGroup group, Integer selectedTahun) {
        if (group.getInisiatifs() == null || group.getInisiatifs().isEmpty()) return null;
        List<RbsiInisiatif> active = group.getInisiatifs().stream()
                .filter(i -> !Boolean.TRUE.equals(i.getIsDeleted()))
                .collect(Collectors.toList());
        if (active.isEmpty()) return null;
        // Prefer exact year match
        return active.stream()
                .filter(i -> selectedTahun.equals(i.getTahun()))
                .findFirst()
                .orElseGet(() -> active.stream()
                        .reduce((a, b) -> a.getTahun() >= b.getTahun() ? a : b)
                        .orElse(null));
    }

    private boolean hasPksiInYear(List<PksiDocument> pksiList, Integer year, Map<UUID, Set<Integer>> pksiTimelineYears) {
        return pksiList.stream().anyMatch(pksi -> isPksiCoveringYear(pksi, year, pksiTimelineYears));
    }

    /**
     * A PKSI covers a year if any of its timeline dates fall in that year.
     * Falls back to legacy date range if no timelines exist.
     */
    private boolean isPksiCoveringYear(PksiDocument pksi, Integer year, Map<UUID, Set<Integer>> pksiTimelineYears) {
        Set<Integer> timelineYears = pksiTimelineYears.get(pksi.getId());
        if (timelineYears != null && !timelineYears.isEmpty()) {
            return timelineYears.contains(year);
        }
        // Legacy fallback: use targetUsreq → targetGoLive range
        Integer startYear = pksi.getTargetUsreq() != null ? pksi.getTargetUsreq().getYear() : null;
        Integer endYear = pksi.getTargetGoLive() != null ? pksi.getTargetGoLive().getYear() : null;
        if (startYear == null && endYear == null) {
            return pksi.getTanggalPengajuan() != null &&
                    pksi.getTanggalPengajuan().getYear() == year;
        }
        if (startYear == null) startYear = year;
        if (endYear == null) endYear = year;
        return year >= startYear && year <= endYear;
    }

    private PksiInfo mapToPksiInfo(PksiDocument pksi, Map<UUID, Set<Integer>> pksiTimelineYears) {
        Set<Integer> years = pksiTimelineYears.get(pksi.getId());
        Integer startYear = years != null && !years.isEmpty() ? years.stream().min(Integer::compareTo).orElse(null) : null;
        Integer endYear   = years != null && !years.isEmpty() ? years.stream().max(Integer::compareTo).orElse(null) : null;
        // Fall back to legacy fields if no timelines
        if (startYear == null) startYear = pksi.getTargetUsreq() != null ? pksi.getTargetUsreq().getYear() : null;
        if (endYear == null)   endYear   = pksi.getTargetGoLive() != null ? pksi.getTargetGoLive().getYear() : null;
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
            List<PksiDocument> groupPksi,
            Map<UUID, Set<Integer>> pksiTimelineYears
    ) {
        Map<Integer, YearlyKepStatus> yearlyStatus = new HashMap<>();
        Map<Integer, KepProgress> groupProgress = kepProgressByGroupAndYear.getOrDefault(groupId, new HashMap<>());

        // Build status for each available year
        for (Integer year : availableYears) {
            KepProgress kp = groupProgress.get(year);
            String kepStatus = kp != null ? kp.getStatus().name() : "none";
            boolean hasPksiInYear = hasPksiInYear(groupPksi, year, pksiTimelineYears);
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
