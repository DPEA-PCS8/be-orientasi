package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.FormasiEfektifRequest;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifDetailResponse;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifDetailResponse.*;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifResponse;
import com.pcs8.orientasi.domain.dto.response.FormasiEfektifResponse.*;
import com.pcs8.orientasi.domain.entity.*;
import com.pcs8.orientasi.domain.entity.PksiDocument.DocumentStatus;
import com.pcs8.orientasi.domain.entity.PksiTimeline.TimelineStage;
import com.pcs8.orientasi.repository.*;
import com.pcs8.orientasi.service.FormasiEfektifService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementation of FormasiEfektifService
 * Handles all Formasi Efektif calculations following DRY principles
 */
@Service
@RequiredArgsConstructor
public class FormasiEfektifServiceImpl implements FormasiEfektifService {

    private static final Logger log = LoggerFactory.getLogger(FormasiEfektifServiceImpl.class);
    private static final String KATEGORI_FORMASI_EFEKTIF = "FORMASI_EFEKTIF";
    private static final String ROLE_PENGEMBANG = "Pengembang";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    // Parameter codes
    private static final String PARAM_WORKING_DAYS = "WORKING_DAYS";
    private static final String PARAM_WORKING_HOURS = "WORKING_HOURS";
    private static final String PARAM_INHOUSE_PCT = "INHOUSE_PCT";
    private static final String PARAM_OUTSOURCE_PCT = "OUTSOURCE_PCT";
    private static final String PARAM_MANAGER_PCT = "MANAGER_PCT";
    private static final String PARAM_ASMAN_PCT = "ASMAN_PCT";
    private static final String PARAM_MAINT_BASE_COUNT = "MAINT_BASE_COUNT";
    private static final String PARAM_MAINT_MGR_PCT = "MAINT_MGR_PCT";
    private static final String PARAM_MAINT_HOURS = "MAINT_HOURS";

    private final MstUserRepository userRepository;
    private final MstVariableRepository variableRepository;
    private final PksiDocumentRepository pksiDocumentRepository;
    private final Fs2DocumentRepository fs2DocumentRepository;
    private final AplikasiSnapshotRepository aplikasiSnapshotRepository;

    /**
     * Configuration parameters holder for reuse across calculations
     */
    private record ConfigParams(
            int workingDays,
            int workingHours,
            double inhousePct,
            double outsourcePct,
            double managerPct,
            double asmanPct,
            int maintBaseCount,
            double maintMgrPct,
            int maintHours
    ) {
        int manHourPerMonth() {
            return workingDays * workingHours;
        }

        int manHourPerYear() {
            return manHourPerMonth() * 12;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public FormasiEfektifResponse getDashboardData(FormasiEfektifRequest request) {
        int selectedTahun = resolveYear(request.getTahun());
        log.info("Getting Formasi Efektif dashboard for year: {}", selectedTahun);

        ConfigParams config = loadConfigParams();
        List<MstUser> developers = getDeveloperUsers();
        List<PksiDocument> pksiList = getFilteredPksiDocuments(selectedTahun);
        List<Fs2Document> fs2List = getFilteredFs2Documents(selectedTahun);

        // Calculate formasi efektif using shared logic
        FormasiByLevel formasiEfektif = calculateFormasiEfektif(pksiList, fs2List, selectedTahun, config);
        FormasiByLevel formasiSaatIni = calculateFormasiSaatIni(developers);
        FormasiByLevel kebutuhan = calculateKebutuhan(formasiEfektif, formasiSaatIni);

        // Build developer list with PKSI count
        List<DeveloperItem> developerList = buildDeveloperList(developers, pksiList, selectedTahun);

        return FormasiEfektifResponse.builder()
                .selectedTahun(selectedTahun)
                .availableYears(getAvailableYears())
                .summary(FormasiSummary.builder()
                        .formasiEfektif(formasiEfektif)
                        .formasiSaatIni(formasiSaatIni)
                        .kebutuhan(kebutuhan)
                        .build())
                .developerList(developerList)
                .parameters(mapToParameterItems(loadVariables()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public FormasiEfektifDetailResponse getDetailData(FormasiEfektifRequest request) {
        int selectedTahun = resolveYear(request.getTahun());
        log.info("Getting Formasi Efektif detail for year: {}", selectedTahun);

        ConfigParams config = loadConfigParams();
        List<MstUser> developers = getDeveloperUsers();
        List<PksiDocument> pksiList = getFilteredPksiDocuments(selectedTahun);
        List<Fs2Document> fs2List = getFilteredFs2Documents(selectedTahun);

        // Calculate individual PKSI/FS2 details
        List<PksiDetailItem> pksiDetails = calculatePksiDetails(pksiList, selectedTahun, config);
        List<Fs2DetailItem> fs2Details = calculateFs2Details(fs2List, selectedTahun, config);

        // Get maintenance base count (active applications)
        int maintenanceBaseCount = getAplikasiAktifCount(selectedTahun, config.maintBaseCount());

        // Calculate man hours by source
        ManHourByLevel pksiManHour = aggregatePksiManHour(pksiDetails);
        ManHourByLevel fs2ManHour = aggregateFs2ManHour(fs2Details);
        ManHourByLevel maintenanceManHour = calculateMaintenanceManHour(selectedTahun, config);
        ManHourByLevel totalManHour = sumManHours(pksiManHour, fs2ManHour, maintenanceManHour);

        // Calculate formasi using shared logic
        FormasiByLevel formasiEfektif = calculateFormasiFromManHour(totalManHour, config);
        FormasiByLevel formasiSaatIni = calculateFormasiSaatIni(developers);
        FormasiByLevel kebutuhan = calculateKebutuhan(formasiEfektif, formasiSaatIni);

        return FormasiEfektifDetailResponse.builder()
                .selectedTahun(selectedTahun)
                .availableYears(getAvailableYears())
                .summary(CalculationSummary.builder()
                        .pksiManHour(pksiManHour)
                        .fs2ManHour(fs2ManHour)
                        .maintenanceManHour(maintenanceManHour)
                        .maintenanceBaseCount(maintenanceBaseCount)
                        .totalManHour(totalManHour)
                        .formasiEfektif(formasiEfektif)
                        .formasiSaatIni(formasiSaatIni)
                        .kebutuhan(kebutuhan)
                        .build())
                .pksiDetails(pksiDetails)
                .fs2Details(fs2Details)
                .parameters(mapToParameterItems(loadVariables()))
                .build();
    }

    @Override
    @Transactional
    public List<ParameterItem> updateParameters(List<ParameterItem> parameters) {
        log.info("Updating {} Formasi Efektif parameters", parameters.size());

        for (ParameterItem param : parameters) {
            variableRepository.findById(UUID.fromString(param.getId()))
                    .ifPresent(variable -> {
                        variable.setNilai(param.getNilai());
                        variableRepository.save(variable);
                    });
        }

        return mapToParameterItems(loadVariables());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParameterItem> getParameters() {
        return mapToParameterItems(loadVariables());
    }

    // ==================== SHARED CALCULATION METHODS (DRY) ====================

    /**
     * Calculate Formasi Efektif from PKSI, FS2, and Maintenance
     */
    private FormasiByLevel calculateFormasiEfektif(List<PksiDocument> pksiList,
                                                    List<Fs2Document> fs2List,
                                                    int tahun,
                                                    ConfigParams config) {
        // Calculate PKSI man hours
        double pksiManajer = 0;
        double pksiAsman = 0;
        for (PksiDocument pksi : pksiList) {
            double[] manHours = calculatePksiManHour(pksi, tahun, config);
            pksiManajer += manHours[0];
            pksiAsman += manHours[1];
        }

        // Calculate FS2 man hours
        double fs2Manajer = 0;
        double fs2Asman = 0;
        for (Fs2Document fs2 : fs2List) {
            double[] manHours = calculateFs2ManHour(fs2, tahun, config);
            fs2Manajer += manHours[0];
            fs2Asman += manHours[1];
        }

        // Calculate maintenance man hours based on active applications
        int maintBaseCount = getAplikasiAktifCount(tahun, config.maintBaseCount());
        double maintManajer = maintBaseCount * (config.maintMgrPct() / 100.0) * config.maintHours();
        double maintAsman = (double) maintBaseCount * config.maintHours();

        // Total man hours
        double totalManajer = pksiManajer + fs2Manajer + maintManajer;
        double totalAsman = pksiAsman + fs2Asman + maintAsman;

        // Convert to formasi (divide by yearly man hour)
        int manHourPerYear = config.manHourPerYear();
        double formasiManajer = totalManajer / manHourPerYear;
        double formasiAsman = totalAsman / manHourPerYear;

        return FormasiByLevel.builder()
                .manajer(roundToTwoDecimals(formasiManajer))
                .asistenManajer(roundToTwoDecimals(formasiAsman))
                .total(roundToTwoDecimals(formasiManajer + formasiAsman))
                .build();
    }

    /**
     * Calculate PKSI man hours: returns [manajer, asman]
     */
    private double[] calculatePksiManHour(PksiDocument pksi, int tahun, ConfigParams config) {
        int durationMonths = calculatePksiDuration(pksi, tahun);
        double manHour = (double) durationMonths * config.manHourPerMonth();
        double workloadPct = getWorkloadPercentage(pksi.getInhouseOutsource(), config);

        double manajer = manHour * workloadPct * (config.managerPct() / 100.0);
        double asman = manHour * workloadPct * (config.asmanPct() / 100.0);

        return new double[]{manajer, asman};
    }

    /**
     * Calculate FS2 man hours: returns [manajer, asman]
     */
    private double[] calculateFs2ManHour(Fs2Document fs2, int tahun, ConfigParams config) {
        int durationMonths = calculateFs2Duration(fs2, tahun);
        double manHour = (double) durationMonths * config.manHourPerMonth();
        double workloadPct = getWorkloadPercentage(fs2.getMekanisme(), config);

        double manajer = manHour * workloadPct * (config.managerPct() / 100.0);
        double asman = manHour * workloadPct * (config.asmanPct() / 100.0);

        return new double[]{manajer, asman};
    }

    /**
     * Calculate PKSI duration in months for a given year
     * Handles cross-year scenarios by capping to the selected year
     */
    private int calculatePksiDuration(PksiDocument pksi, int tahun) {
        List<PksiTimeline> timelines = pksi.getTimelines();
        if (timelines == null || timelines.isEmpty()) {
            return 0;
        }

        // Find furthest USREQ and UAT dates (handle multiple phases)
        LocalDate usreqDate = findFurthestDateByStage(timelines, TimelineStage.USREQ);
        LocalDate uatDate = findFurthestDateByStage(timelines, TimelineStage.UAT);

        if (usreqDate == null || uatDate == null) {
            return 0;
        }

        // Apply year boundaries
        LocalDate yearStart = LocalDate.of(tahun, 1, 1);
        LocalDate yearEnd = LocalDate.of(tahun, 12, 31);

        // Adjust dates to fit within the selected year
        LocalDate effectiveStart = usreqDate.isBefore(yearStart) ? yearStart : usreqDate;
        LocalDate effectiveEnd = uatDate.isAfter(yearEnd) ? yearEnd : uatDate;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0;
        }

        // Calculate months difference
        long months = ChronoUnit.MONTHS.between(effectiveStart.withDayOfMonth(1), effectiveEnd.withDayOfMonth(1)) + 1;
        return Math.min((int) months, 12);
    }

    /**
     * Calculate FS2 duration in months for a given year
     */
    private int calculateFs2Duration(Fs2Document fs2, int tahun) {
        LocalDate pengajuan = fs2.getTanggalPengajuan();
        LocalDate goLive = fs2.getTargetGoLive();

        if (pengajuan == null || goLive == null) {
            return 0;
        }

        LocalDate yearStart = LocalDate.of(tahun, 1, 1);
        LocalDate yearEnd = LocalDate.of(tahun, 12, 31);

        LocalDate effectiveStart = pengajuan.isBefore(yearStart) ? yearStart : pengajuan;
        LocalDate effectiveEnd = goLive.isAfter(yearEnd) ? yearEnd : goLive;

        if (effectiveStart.isAfter(effectiveEnd)) {
            return 0;
        }

        long months = ChronoUnit.MONTHS.between(effectiveStart.withDayOfMonth(1), effectiveEnd.withDayOfMonth(1)) + 1;
        return Math.min((int) months, 12);
    }

    /**
     * Find the furthest date for a given stage from timeline list
     */
    private LocalDate findFurthestDateByStage(List<PksiTimeline> timelines, TimelineStage stage) {
        return timelines.stream()
                .filter(t -> t.getStage() == stage)
                .map(PksiTimeline::getTargetDate)
                .filter(Objects::nonNull)
                .max(LocalDate::compareTo)
                .orElse(null);
    }

    /**
     * Get workload percentage based on inhouse/outsource
     */
    private double getWorkloadPercentage(String type, ConfigParams config) {
        if (type == null) {
            return config.inhousePct() / 100.0;
        }
        return type.toUpperCase().contains("OUTSOURCE")
                ? config.outsourcePct() / 100.0
                : config.inhousePct() / 100.0;
    }

    /**
     * Calculate Formasi Saat Ini from active developers
     */
    private FormasiByLevel calculateFormasiSaatIni(List<MstUser> developers) {
        long manajerCount = developers.stream()
                .filter(u -> isManagerLevel(u.getTitle()))
                .count();
        long asmanCount = developers.stream()
                .filter(u -> isAsmanLevel(u.getTitle()))
                .count();

        return FormasiByLevel.builder()
                .manajer((double) manajerCount)
                .asistenManajer((double) asmanCount)
                .total((double) (manajerCount + asmanCount))
                .build();
    }

    /**
     * Calculate gap (Kebutuhan)
     */
    private FormasiByLevel calculateKebutuhan(FormasiByLevel efektif, FormasiByLevel saatIni) {
        double manajerGap = efektif.getManajer() - saatIni.getManajer();
        double asmanGap = efektif.getAsistenManajer() - saatIni.getAsistenManajer();

        return FormasiByLevel.builder()
                .manajer(roundToTwoDecimals(manajerGap))
                .asistenManajer(roundToTwoDecimals(asmanGap))
                .total(roundToTwoDecimals(manajerGap + asmanGap))
                .build();
    }

    /**
     * Convert total man hours to formasi
     */
    private FormasiByLevel calculateFormasiFromManHour(ManHourByLevel manHour, ConfigParams config) {
        int manHourPerYear = config.manHourPerYear();
        return FormasiByLevel.builder()
                .manajer(roundToTwoDecimals(manHour.getManajer() / manHourPerYear))
                .asistenManajer(roundToTwoDecimals(manHour.getAsistenManajer() / manHourPerYear))
                .total(roundToTwoDecimals(manHour.getTotal() / manHourPerYear))
                .build();
    }

    // ==================== DETAIL CALCULATION METHODS ====================

    private List<PksiDetailItem> calculatePksiDetails(List<PksiDocument> pksiList, int tahun, ConfigParams config) {
        return pksiList.stream().map(pksi -> {
            int duration = calculatePksiDuration(pksi, tahun);
            double manHourBase = (double) duration * config.manHourPerMonth();
            double workloadPct = getWorkloadPercentage(pksi.getInhouseOutsource(), config) * 100.0;
            double manHour = manHourBase * (workloadPct / 100);

            LocalDate usreq = findFurthestDateByStage(pksi.getTimelines(), TimelineStage.USREQ);
            LocalDate uat = findFurthestDateByStage(pksi.getTimelines(), TimelineStage.UAT);

            return PksiDetailItem.builder()
                    .id(pksi.getId().toString())
                    .namaPksi(pksi.getNamaPksi())
                    .namaAplikasi(pksi.getAplikasi() != null ? pksi.getAplikasi().getNamaAplikasi() : null)
                    .jenisPksi(pksi.getJenisPksi())
                    .inhouseOutsource(pksi.getInhouseOutsource())
                    .workloadPct(workloadPct)
                    .usreqDate(usreq != null ? usreq.format(DATE_FORMATTER) : null)
                    .uatDate(uat != null ? uat.format(DATE_FORMATTER) : null)
                    .durationMonths(duration)
                    .manHour(roundToTwoDecimals(manHour))
                    .manHourManajer(roundToTwoDecimals(manHour * (config.managerPct() / 100.0)))
                    .manHourAsman(roundToTwoDecimals(manHour * (config.asmanPct() / 100.0)))
                    .build();
        }).collect(Collectors.toList());
    }

    private List<Fs2DetailItem> calculateFs2Details(List<Fs2Document> fs2List, int tahun, ConfigParams config) {
        return fs2List.stream().map(fs2 -> {
            int duration = calculateFs2Duration(fs2, tahun);
            double manHourBase = (double) duration * config.manHourPerMonth();
            double workloadPct = getWorkloadPercentage(fs2.getMekanisme(), config) * 100.0;
            double manHour = manHourBase * (workloadPct / 100);

            return Fs2DetailItem.builder()
                    .id(fs2.getId().toString())
                    .namaAplikasi(fs2.getAplikasi() != null ? fs2.getAplikasi().getNamaAplikasi() : null)
                    .deskripsiPengubahan(fs2.getDeskripsiPengubahan())
                    .mekanisme(fs2.getMekanisme())
                    .workloadPct(workloadPct)
                    .tanggalPengajuan(fs2.getTanggalPengajuan() != null ? fs2.getTanggalPengajuan().format(DATE_FORMATTER) : null)
                    .targetGoLive(fs2.getTargetGoLive() != null ? fs2.getTargetGoLive().format(DATE_FORMATTER) : null)
                    .durationMonths(duration)
                    .manHour(roundToTwoDecimals(manHour))
                    .manHourManajer(roundToTwoDecimals(manHour * (config.managerPct() / 100.0)))
                    .manHourAsman(roundToTwoDecimals(manHour * (config.asmanPct() / 100.0)))
                    .build();
        }).collect(Collectors.toList());
    }

    private ManHourByLevel aggregatePksiManHour(List<PksiDetailItem> details) {
        double manajer = details.stream().mapToDouble(PksiDetailItem::getManHourManajer).sum();
        double asman = details.stream().mapToDouble(PksiDetailItem::getManHourAsman).sum();
        return ManHourByLevel.builder()
                .manajer(roundToTwoDecimals(manajer))
                .asistenManajer(roundToTwoDecimals(asman))
                .total(roundToTwoDecimals(manajer + asman))
                .build();
    }

    private ManHourByLevel aggregateFs2ManHour(List<Fs2DetailItem> details) {
        double manajer = details.stream().mapToDouble(Fs2DetailItem::getManHourManajer).sum();
        double asman = details.stream().mapToDouble(Fs2DetailItem::getManHourAsman).sum();
        return ManHourByLevel.builder()
                .manajer(roundToTwoDecimals(manajer))
                .asistenManajer(roundToTwoDecimals(asman))
                .total(roundToTwoDecimals(manajer + asman))
                .build();
    }

    private ManHourByLevel calculateMaintenanceManHour(int tahun, ConfigParams config) {
        int maintBaseCount = getAplikasiAktifCount(tahun, config.maintBaseCount());
        double manajer = maintBaseCount * (config.maintMgrPct() / 100.0) * config.maintHours();
        double asman = (double) maintBaseCount * config.maintHours();
        return ManHourByLevel.builder()
                .manajer(roundToTwoDecimals(manajer))
                .asistenManajer(roundToTwoDecimals(asman))
                .total(roundToTwoDecimals(manajer + asman))
                .build();
    }

    /**
     * Get count of active applications for a specific year
     * Falls back to default value if no snapshot data exists
     */
    private int getAplikasiAktifCount(int tahun, int defaultValue) {
        try {
            Long count = aplikasiSnapshotRepository.countAktifByTahun(tahun);
            if (count != null && count > 0) {
                log.info("Using {} active applications from snapshot for year {}", count, tahun);
                return count.intValue();
            }
            log.info("No active application snapshot found for year {}, using default value: {}", tahun, defaultValue);
            return defaultValue;
        } catch (Exception e) {
            log.warn("Failed to fetch active application count for year {}, using default value: {}", tahun, defaultValue, e);
            return defaultValue;
        }
    }

    private ManHourByLevel sumManHours(ManHourByLevel... sources) {
        double manajer = Arrays.stream(sources).mapToDouble(ManHourByLevel::getManajer).sum();
        double asman = Arrays.stream(sources).mapToDouble(ManHourByLevel::getAsistenManajer).sum();
        return ManHourByLevel.builder()
                .manajer(roundToTwoDecimals(manajer))
                .asistenManajer(roundToTwoDecimals(asman))
                .total(roundToTwoDecimals(manajer + asman))
                .build();
    }

    // ==================== DATA LOADING METHODS ====================

    private ConfigParams loadConfigParams() {
        Map<String, String> params = loadVariables().stream()
                .collect(Collectors.toMap(MstVariable::getKode, v -> v.getNilai() != null ? v.getNilai() : "0"));

        return new ConfigParams(
                parseInt(params.getOrDefault(PARAM_WORKING_DAYS, "22")),
                parseInt(params.getOrDefault(PARAM_WORKING_HOURS, "8")),
                parseDouble(params.getOrDefault(PARAM_INHOUSE_PCT, "100")),
                parseDouble(params.getOrDefault(PARAM_OUTSOURCE_PCT, "30")),
                parseDouble(params.getOrDefault(PARAM_MANAGER_PCT, "80")),
                parseDouble(params.getOrDefault(PARAM_ASMAN_PCT, "100")),
                parseInt(params.getOrDefault(PARAM_MAINT_BASE_COUNT, "86")),
                parseDouble(params.getOrDefault(PARAM_MAINT_MGR_PCT, "50")),
                parseInt(params.getOrDefault(PARAM_MAINT_HOURS, "80"))
        );
    }

    private List<MstVariable> loadVariables() {
        return variableRepository.findByKategoriAndIsActiveTrueOrderByUrutanAscNamaAsc(KATEGORI_FORMASI_EFEKTIF);
    }

    private List<MstUser> getDeveloperUsers() {
        return userRepository.findByRoleName(ROLE_PENGEMBANG).stream()
                .filter(u -> isManagerLevel(u.getTitle()) || isAsmanLevel(u.getTitle()))
                .collect(Collectors.toList());
    }

    private List<PksiDocument> getFilteredPksiDocuments(int tahun) {
        List<PksiDocument> pksiList = pksiDocumentRepository.findAll().stream()
                .filter(pksi -> pksi.getStatus() == DocumentStatus.DISETUJUI)
                .filter(pksi -> hasPksiTimelineInYear(pksi, tahun))
                .collect(Collectors.toList());
        
        // Force initialization of team and teamMembers for PKSI count calculation
        pksiList.forEach(pksi -> {
            if (pksi.getTeam() != null) {
                pksi.getTeam().getId(); // Force initialization
                if (pksi.getTeam().getTeamMembers() != null) {
                    pksi.getTeam().getTeamMembers().size(); // Force initialization
                }
            }
        });
        
        return pksiList;
    }

    private boolean hasPksiTimelineInYear(PksiDocument pksi, int tahun) {
        if (pksi.getTimelines() == null || pksi.getTimelines().isEmpty()) {
            return false;
        }
        return pksi.getTimelines().stream()
                .filter(t -> t.getStage() == TimelineStage.USREQ ||
                             t.getStage() == TimelineStage.SIT ||
                             t.getStage() == TimelineStage.UAT ||
                             t.getStage() == TimelineStage.GO_LIVE)
                .anyMatch(t -> t.getTargetDate() != null && t.getTargetDate().getYear() == tahun);
    }

    private List<Fs2Document> getFilteredFs2Documents(int tahun) {
        List<Fs2Document> fs2List = fs2DocumentRepository.findByStatusOrderByCreatedAtDesc("DISETUJUI").stream()
                .filter(fs2 -> fs2.getTanggalPengajuan() != null && fs2.getTanggalPengajuan().getYear() == tahun)
                .collect(Collectors.toList());
        
        // Force initialization of team and teamMembers for FS2 count calculation
        fs2List.forEach(fs2 -> {
            if (fs2.getTeam() != null) {
                fs2.getTeam().getId(); // Force initialization
                if (fs2.getTeam().getTeamMembers() != null) {
                    fs2.getTeam().getTeamMembers().size(); // Force initialization
                }
            }
            // Force load aplikasi for name
            if (fs2.getAplikasi() != null) {
                fs2.getAplikasi().getNamaAplikasi();
            }
        });
        
        return fs2List;
    }

    // ==================== HELPER METHODS ====================

    private int resolveYear(Integer tahun) {
        return tahun != null ? tahun : LocalDate.now().getYear();
    }

    private List<Integer> getAvailableYears() {
        int currentYear = LocalDate.now().getYear();
        List<Integer> years = new ArrayList<>();
        for (int i = currentYear - 2; i <= currentYear + 2; i++) {
            years.add(i);
        }
        return years;
    }

    private boolean isManagerLevel(String title) {
        if (title == null) return false;
        String lower = title.toLowerCase();
        return lower.contains("manajer") && !lower.contains("asisten");
    }

    private boolean isAsmanLevel(String title) {
        if (title == null) return false;
        String lower = title.toLowerCase();
        return lower.contains("asisten") && lower.contains("manajer");
    }

    private List<DeveloperItem> buildDeveloperList(List<MstUser> developers, List<PksiDocument> pksiList, int tahun) {
        // Get FS2 list for the year
        List<Fs2Document> fs2List = getFilteredFs2Documents(tahun);

        // Build map of team ID to PKSI list
        Map<UUID, List<PksiDocument>> pksiByTeam = pksiList.stream()
                .filter(pksi -> pksi.getTeam() != null)
                .collect(Collectors.groupingBy(pksi -> pksi.getTeam().getId()));

        // Build map of team ID to FS2 list
        Map<UUID, List<Fs2Document>> fs2ByTeam = fs2List.stream()
                .filter(fs2 -> fs2.getTeam() != null)
                .collect(Collectors.groupingBy(fs2 -> fs2.getTeam().getId()));

        // Build map of user ID to team IDs (user can be in multiple teams)
        Map<UUID, Set<UUID>> teamsByUser = new HashMap<>();
        for (PksiDocument pksi : pksiList) {
            if (pksi.getTeam() != null && pksi.getTeam().getTeamMembers() != null) {
                UUID teamId = pksi.getTeam().getId();
                for (MstTeamMember member : pksi.getTeam().getTeamMembers()) {
                    UUID userId = member.getUser().getUuid();
                    teamsByUser.computeIfAbsent(userId, k -> new HashSet<>()).add(teamId);
                }
            }
        }
        // Also add teams from FS2
        for (Fs2Document fs2 : fs2List) {
            if (fs2.getTeam() != null && fs2.getTeam().getTeamMembers() != null) {
                UUID teamId = fs2.getTeam().getId();
                for (MstTeamMember member : fs2.getTeam().getTeamMembers()) {
                    UUID userId = member.getUser().getUuid();
                    teamsByUser.computeIfAbsent(userId, k -> new HashSet<>()).add(teamId);
                }
            }
        }

        return developers.stream().map(user -> {
            String level = isManagerLevel(user.getTitle()) ? "MANAJER" :
                          isAsmanLevel(user.getTitle()) ? "ASISTEN_MANAJER" : "LAINNYA";

            Set<UUID> userTeams = teamsByUser.getOrDefault(user.getUuid(), Collections.emptySet());

            // Build PKSI list for this user
            List<FormasiEfektifResponse.WorkItem> pksiWorkList = userTeams.stream()
                    .flatMap(teamId -> pksiByTeam.getOrDefault(teamId, Collections.emptyList()).stream()
                            .map(pksi -> FormasiEfektifResponse.WorkItem.builder()
                                    .id(pksi.getId().toString())
                                    .name(pksi.getNamaPksi())
                                    .teamName(pksi.getTeam() != null ? pksi.getTeam().getName() : null)
                                    .build()))
                    .collect(Collectors.toList());

            // Build FS2 list for this user
            List<FormasiEfektifResponse.WorkItem> fs2WorkList = userTeams.stream()
                    .flatMap(teamId -> fs2ByTeam.getOrDefault(teamId, Collections.emptyList()).stream()
                            .map(fs2 -> FormasiEfektifResponse.WorkItem.builder()
                                    .id(fs2.getId().toString())
                                    .name(fs2.getAplikasi() != null ? 
                                          "FS2 - " + fs2.getAplikasi().getNamaAplikasi() : 
                                          "FS2 Document")
                                    .teamName(fs2.getTeam() != null ? fs2.getTeam().getName() : null)
                                    .build()))
                    .collect(Collectors.toList());

            return DeveloperItem.builder()
                    .id(user.getUuid().toString())
                    .fullName(user.getFullName())
                    .username(user.getUsername())
                    .title(user.getTitle())
                    .level(level)
                    .pksiCount(pksiWorkList.size())
                    .fs2Count(fs2WorkList.size())
                    .pksiList(pksiWorkList)
                    .fs2List(fs2WorkList)
                    .build();
        }).collect(Collectors.toList());
    }

    private List<ParameterItem> mapToParameterItems(List<MstVariable> variables) {
        return variables.stream().map(v -> ParameterItem.builder()
                .id(v.getId().toString())
                .kode(v.getKode())
                .nama(v.getNama())
                .deskripsi(v.getDeskripsi())
                .nilai(v.getNilai())
                .urutan(v.getUrutan())
                .build()
        ).collect(Collectors.toList());
    }

    private int parseInt(String value) {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private double parseDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    private double roundToTwoDecimals(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}
