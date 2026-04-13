package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.PksiDashboardRequest;
import com.pcs8.orientasi.domain.dto.response.PksiDashboardResponse;
import com.pcs8.orientasi.domain.dto.response.PksiDashboardResponse.*;
import com.pcs8.orientasi.domain.entity.MstBidang;
import com.pcs8.orientasi.domain.entity.MstSkpa;
import com.pcs8.orientasi.domain.entity.PksiChangelog;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.domain.entity.PksiDocument.DocumentStatus;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.repository.PksiChangelogRepository;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.service.PksiDashboardService;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PksiDashboardServiceImpl implements PksiDashboardService {

    private static final Logger log = LoggerFactory.getLogger(PksiDashboardServiceImpl.class);

    private static final List<String> PROGRESS_STAGES = Arrays.asList(
            "Penyusunan Usreq", "Pengadaan", "Desain", "Coding", 
            "Unit Test", "SIT", "UAT", "Deployment", "Selesai"
    );

    private static final List<String> EARLY_STAGE = Arrays.asList("Penyusunan Usreq", "Pengadaan");
    private static final List<String> DEV_STAGE = Arrays.asList("Desain", "Coding", "Unit Test");
    private static final List<String> TEST_STAGE = Arrays.asList("SIT", "UAT");
    private static final List<String> DEPLOY_STAGE = Arrays.asList("Deployment", "Selesai");

    private static final String[] MONTH_NAMES = {
        "", "Januari", "Februari", "Maret", "April", "Mei", "Juni",
        "Juli", "Agustus", "September", "Oktober", "November", "Desember"
    };

    private final PksiDocumentRepository pksiDocumentRepository;
    private final PksiChangelogRepository pksiChangelogRepository;
    private final MstSkpaRepository skpaRepository;
    private final MstBidangRepository bidangRepository;

    @Override
    @Transactional(readOnly = true)
    public PksiDashboardResponse getDashboardData(PksiDashboardRequest request) {
        int currentYear = LocalDate.now().getYear();
        int currentMonth = LocalDate.now().getMonthValue();
        
        Integer selectedTahun = request.getTahun() != null ? request.getTahun() : currentYear;
        Integer selectedBulan = request.getBulan() != null ? request.getBulan() : 
                (selectedTahun == currentYear ? currentMonth : 12);

        log.info("Getting PKSI dashboard data for tahun: {}, bulan: {}", selectedTahun, selectedBulan);

        // Build snapshot date (end of selected month)
        LocalDateTime snapshotDateTime = YearMonth.of(selectedTahun, selectedBulan)
                .atEndOfMonth().atTime(23, 59, 59);
        String snapshotDate = snapshotDateTime.format(DateTimeFormatter.ofPattern("d MMMM yyyy", new Locale("id", "ID")));

        // Load SKPA and Bidang mappings
        List<MstSkpa> allSkpa = skpaRepository.findAllByOrderByKodeSkpaAsc();
        allSkpa.forEach(skpa -> Hibernate.initialize(skpa.getBidang()));
        Map<UUID, MstSkpa> skpaMap = allSkpa.stream()
                .collect(Collectors.toMap(MstSkpa::getId, s -> s));

        List<MstBidang> allBidang = bidangRepository.findAllByOrderByKodeBidangAsc();
        List<BidangItem> bidangList = allBidang.stream()
                .map(b -> BidangItem.builder()
                        .id(b.getId())
                        .kodeBidang(b.getKodeBidang())
                        .namaBidang(b.getNamaBidang())
                        .build())
                .collect(Collectors.toList());

        // Get all PKSI documents
        List<PksiDocument> allDocuments = pksiDocumentRepository.findAllWithUser();
        
        // Filter by year
        List<PksiDocument> filteredDocuments = filterByYear(allDocuments, selectedTahun);

        // Filter by month - only include documents created before or at the end of selected month
        List<PksiDocument> documentsInSnapshot = filteredDocuments.stream()
                .filter(p -> p.getCreatedAt() != null && p.getCreatedAt().isBefore(snapshotDateTime.plusSeconds(1)))
                .collect(Collectors.toList());

        // Get historical progress state at snapshot date
        Map<UUID, String> historicalProgress = getHistoricalProgressState(documentsInSnapshot, snapshotDateTime);

        // Build available years & months
        List<Integer> availableYears = extractAvailableYears(allDocuments);
        List<MonthOption> availableMonths = buildMonthOptions();

        // Calculate summary - using snapshot filtered documents
        DashboardSummary summary = calculateSummary(documentsInSnapshot);

        // Filter approved PKSI from snapshot
        List<PksiDocument> approvedDocuments = documentsInSnapshot.stream()
                .filter(p -> p.getStatus() == DocumentStatus.DISETUJUI)
                .collect(Collectors.toList());

        // Calculate various insights
        ApprovalBreakdown approvalBreakdown = calculateApprovalBreakdown(approvedDocuments, selectedTahun);
        List<ProgressByBidangRow> progressByBidang = calculateProgressByBidang(approvedDocuments, allBidang, skpaMap, historicalProgress);
        ProgressInsights progressInsights = calculateProgressInsights(approvedDocuments, selectedTahun, historicalProgress);
        JenisPksiStats jenisPksiStats = calculateJenisPksiStats(approvedDocuments, selectedTahun);
        PelaksanaStats pelaksanaStats = calculatePelaksanaStats(approvedDocuments);
        List<BidangStat> bidangStats = calculateBidangStats(approvedDocuments, allBidang, skpaMap);
        List<PksiListItem> pksiList = buildPksiList(documentsInSnapshot, skpaMap, historicalProgress);
        List<MonthlyProgressTrend> monthlyTrend = calculateMonthlyTrend(approvedDocuments, selectedTahun);

        return PksiDashboardResponse.builder()
                .selectedTahun(selectedTahun)
                .selectedBulan(selectedBulan)
                .availableYears(availableYears)
                .availableMonths(availableMonths)
                .snapshotDate(snapshotDate)
                .summary(summary)
                .approvalBreakdown(approvalBreakdown)
                .progressByBidang(progressByBidang)
                .bidangList(bidangList)
                .progressInsights(progressInsights)
                .jenisPksiStats(jenisPksiStats)
                .pelaksanaStats(pelaksanaStats)
                .bidangStats(bidangStats)
                .pksiList(pksiList)
                .monthlyProgressTrend(monthlyTrend)
                .build();
    }

    private Map<UUID, String> getHistoricalProgressState(List<PksiDocument> documents, LocalDateTime snapshotDateTime) {
        Map<UUID, String> result = new HashMap<>();
        
        // Get last progress change before snapshot date for all PKSIs
        List<PksiChangelog> progressChanges = pksiChangelogRepository.findLastProgressChangeBeforeDate(snapshotDateTime);
        
        for (PksiChangelog change : progressChanges) {
            if (change.getPksiDocument() != null) {
                result.put(change.getPksiDocument().getId(), change.getNewValue());
            }
        }
        
        // For documents without changelog, use current progress if created before snapshot
        for (PksiDocument doc : documents) {
            if (!result.containsKey(doc.getId())) {
                if (doc.getCreatedAt() != null && doc.getCreatedAt().isBefore(snapshotDateTime)) {
                    result.put(doc.getId(), doc.getProgress());
                }
            }
        }
        
        return result;
    }

    private List<MonthOption> buildMonthOptions() {
        List<MonthOption> months = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            months.add(MonthOption.builder()
                    .value(i)
                    .label(MONTH_NAMES[i])
                    .build());
        }
        return months;
    }

    private List<PksiDocument> filterByYear(List<PksiDocument> documents, Integer year) {
        if (year == null) return documents;
        return documents.stream()
                .filter(p -> isInYear(p, year))
                .collect(Collectors.toList());
    }

    private boolean isInYear(PksiDocument doc, int year) {
        return checkYearMatch(doc.getTargetUsreq(), year) ||
               checkYearMatch(doc.getTargetSit(), year) ||
               checkYearMatch(doc.getTargetUat(), year) ||
               checkYearMatch(doc.getTargetGoLive(), year);
    }

    private boolean checkYearMatch(LocalDate date, int year) {
        return date != null && date.getYear() == year;
    }

    private List<Integer> extractAvailableYears(List<PksiDocument> documents) {
        Set<Integer> years = new TreeSet<>();
        for (PksiDocument doc : documents) {
            addYearIfPresent(years, doc.getTargetUsreq());
            addYearIfPresent(years, doc.getTargetSit());
            addYearIfPresent(years, doc.getTargetUat());
            addYearIfPresent(years, doc.getTargetGoLive());
        }
        return new ArrayList<>(years);
    }

    private void addYearIfPresent(Set<Integer> years, LocalDate date) {
        if (date != null) years.add(date.getYear());
    }

    private DashboardSummary calculateSummary(List<PksiDocument> documents) {
        int total = documents.size();
        int disetujui = (int) documents.stream().filter(p -> p.getStatus() == DocumentStatus.DISETUJUI).count();
        int pending = (int) documents.stream().filter(p -> p.getStatus() == DocumentStatus.PENDING).count();
        int ditolak = (int) documents.stream().filter(p -> p.getStatus() == DocumentStatus.DITOLAK).count();
        double percentageDisetujui = total > 0 ? Math.round((disetujui * 100.0 / total) * 10.0) / 10.0 : 0.0;

        return DashboardSummary.builder()
                .totalPksi(total)
                .totalDisetujui(disetujui)
                .totalPending(pending)
                .totalDitolak(ditolak)
                .percentageDisetujui(percentageDisetujui)
                .build();
    }

    private ApprovalBreakdown calculateApprovalBreakdown(List<PksiDocument> approvedDocuments, int selectedTahun) {
        int disetujuiTahunIni = 0;
        int disetujuiMultiyearsSebelumnya = 0;

        // One More Type: PKSI Mendesak
        for (PksiDocument doc : approvedDocuments) {
            LocalDate targetGoLive = doc.getTargetGoLive();
            if (targetGoLive != null && targetGoLive.getYear() == selectedTahun) {
                disetujuiTahunIni++;
            } else if (targetGoLive != null && targetGoLive.getYear() == selectedTahun - 1) {
                disetujuiMultiyearsSebelumnya++;
            } else {
                log.warn("PKSI {} has approved status but targetGoLive year is unknown or in the future", doc.getId());
            }
        }

        return ApprovalBreakdown.builder()
                .disetujuiTahunIni(disetujuiTahunIni)
                .disetujuiMultiyearsSebelumnya(disetujuiMultiyearsSebelumnya)
                .build();
    }

    private List<ProgressByBidangRow> calculateProgressByBidang(
            List<PksiDocument> approvedDocuments,
            List<MstBidang> allBidang,
            Map<UUID, MstSkpa> skpaMap,
            Map<UUID, String> historicalProgress) {

        List<ProgressByBidangRow> result = new ArrayList<>();

        for (String progress : PROGRESS_STAGES) {
            Map<String, Integer> countsByBidang = new LinkedHashMap<>();
            for (MstBidang bidang : allBidang) {
                countsByBidang.put(bidang.getKodeBidang(), 0);
            }

            int total = 0;
            for (PksiDocument doc : approvedDocuments) {
                String docProgress = historicalProgress.getOrDefault(doc.getId(), doc.getProgress());
                if (progress.equals(docProgress)) {
                    String bidangKode = resolveBidangKode(doc.getPicSatker(), skpaMap);
                    if (bidangKode != null && countsByBidang.containsKey(bidangKode)) {
                        countsByBidang.merge(bidangKode, 1, Integer::sum);
                    }
                    total++;
                }
            }

            result.add(ProgressByBidangRow.builder()
                    .progress(progress)
                    .progressLabel(progress)
                    .countsByBidang(countsByBidang)
                    .total(total)
                    .build());
        }

        return result;
    }

    private ProgressInsights calculateProgressInsights(
            List<PksiDocument> approvedDocuments, 
            int selectedTahun,
            Map<UUID, String> historicalProgress) {
        
        int totalApproved = approvedDocuments.size();

        // Calculate phase counts
        PhaseDetail earlyStage = calculatePhaseDetail("Tahap Awal", EARLY_STAGE, approvedDocuments, totalApproved, historicalProgress);
        PhaseDetail devStage = calculatePhaseDetail("Pengembangan", DEV_STAGE, approvedDocuments, totalApproved, historicalProgress);
        PhaseDetail testStage = calculatePhaseDetail("Pengujian", TEST_STAGE, approvedDocuments, totalApproved, historicalProgress);
        PhaseDetail deployStage = calculatePhaseDetail("Finalisasi", DEPLOY_STAGE, approvedDocuments, totalApproved, historicalProgress);

        // Calculate deadline insights
        DeadlineInsight deadlineCurrentYear = calculateDeadlineInsight(approvedDocuments, selectedTahun, historicalProgress);
        DeadlineInsight deadlineNextYear = calculateDeadlineInsight(approvedDocuments, selectedTahun + 1, historicalProgress);

        return ProgressInsights.builder()
                .earlyStage(earlyStage)
                .developmentStage(devStage)
                .testingStage(testStage)
                .deploymentStage(deployStage)
                .deadlineCurrentYear(deadlineCurrentYear)
                .deadlineNextYear(deadlineNextYear)
                .build();
    }

    private PhaseDetail calculatePhaseDetail(
            String label, 
            List<String> progressList, 
            List<PksiDocument> documents,
            int totalApproved,
            Map<UUID, String> historicalProgress) {
        
        List<ProgressCount> breakdown = new ArrayList<>();
        int phaseTotal = 0;

        for (String progress : progressList) {
            int count = 0;
            for (PksiDocument doc : documents) {
                String docProgress = historicalProgress.getOrDefault(doc.getId(), doc.getProgress());
                if (progress.equals(docProgress)) {
                    count++;
                }
            }
            breakdown.add(ProgressCount.builder().progress(progress).count(count).build());
            phaseTotal += count;
        }

        double percentage = totalApproved > 0 ? Math.round((phaseTotal * 100.0 / totalApproved) * 10.0) / 10.0 : 0.0;

        return PhaseDetail.builder()
                .label(label)
                .total(phaseTotal)
                .percentage(percentage)
                .progressBreakdown(breakdown)
                .build();
    }

    private DeadlineInsight calculateDeadlineInsight(
            List<PksiDocument> approvedDocuments, 
            int targetYear,
            Map<UUID, String> historicalProgress) {
        
        List<ProgressCount> breakdown = new ArrayList<>();
        int deadlineTotal = 0;

        Map<String, Integer> progressCounts = new LinkedHashMap<>();
        for (String progress : PROGRESS_STAGES) {
            progressCounts.put(progress, 0);
        }

        for (PksiDocument doc : approvedDocuments) {
            LocalDate deadline = doc.getTargetGoLive();
            if (deadline != null && deadline.getYear() == targetYear) {
                deadlineTotal++;
                String docProgress = historicalProgress.getOrDefault(doc.getId(), doc.getProgress());
                if (docProgress != null && progressCounts.containsKey(docProgress)) {
                    progressCounts.merge(docProgress, 1, Integer::sum);
                }
            }
        }

        for (Map.Entry<String, Integer> entry : progressCounts.entrySet()) {
            if (entry.getValue() > 0) {
                breakdown.add(ProgressCount.builder()
                        .progress(entry.getKey())
                        .count(entry.getValue())
                        .build());
            }
        }

        String label = deadlineTotal > 0 
            ? String.format("%d PKSI harus selesai di %d", deadlineTotal, targetYear)
            : String.format("Tidak ada PKSI dengan deadline %d", targetYear);

        return DeadlineInsight.builder()
                .year(targetYear)
                .total(deadlineTotal)
                .label(label)
                .progressBreakdown(breakdown)
                .build();
    }

    private JenisPksiStats calculateJenisPksiStats(List<PksiDocument> approvedDocuments, int selectedTahun) {
        int singleYear = 0;
        int multiyearsYMinus1 = 0;
        int multiyearsYPlus1 = 0;

        for (PksiDocument doc : approvedDocuments) {
            if (isMultiyear(doc)) {
                // Multiyear PKSI
                Integer startYear = getStartYear(doc);
                Integer endYear = getEndYear(doc);
                
                if (startYear != null && startYear <= selectedTahun - 1) {
                    multiyearsYMinus1++;
                } else if (endYear != null && endYear >= selectedTahun + 1) {
                    multiyearsYPlus1++;
                }
            } else {
                // Single year PKSI
                LocalDate targetGoLive = doc.getTargetGoLive();
                if (targetGoLive != null && targetGoLive.getYear() == selectedTahun) {
                    singleYear++;
                }
            }
        }

        return JenisPksiStats.builder()
                .singleYear(singleYear)
                .multiyearsYMinus1(multiyearsYMinus1)
                .multiyearsYPlus1(multiyearsYPlus1)
                .build();
    }

    private boolean isMultiyear(PksiDocument doc) {
        Integer startYear = getStartYear(doc);
        Integer endYear = getEndYear(doc);
        return startYear != null && endYear != null && !startYear.equals(endYear);
    }

    private Integer getStartYear(PksiDocument doc) {
        if (doc.getTargetUsreq() != null) return doc.getTargetUsreq().getYear();
        if (doc.getTargetSit() != null) return doc.getTargetSit().getYear();
        if (doc.getTargetUat() != null) return doc.getTargetUat().getYear();
        if (doc.getTargetGoLive() != null) return doc.getTargetGoLive().getYear();
        return null;
    }

    private Integer getEndYear(PksiDocument doc) {
        if (doc.getTargetGoLive() != null) return doc.getTargetGoLive().getYear();
        return null;
    }

    private PelaksanaStats calculatePelaksanaStats(List<PksiDocument> approvedDocuments) {
        int inhouse = 0;
        int outsource = 0;
        int unknown = 0;

        for (PksiDocument doc : approvedDocuments) {
            String io = doc.getInhouseOutsource();
            if (io == null || io.trim().isEmpty()) {
                unknown++;
            } else if (io.toLowerCase().contains("inhouse")) {
                inhouse++;
            } else if (io.toLowerCase().contains("outsource")) {
                outsource++;
            } else {
                unknown++;
            }
        }

        return PelaksanaStats.builder()
                .inhouse(inhouse)
                .outsource(outsource)
                .unknown(unknown)
                .build();
    }

    private List<BidangStat> calculateBidangStats(
            List<PksiDocument> approvedDocuments,
            List<MstBidang> allBidang,
            Map<UUID, MstSkpa> skpaMap) {
        
        // Count PKSI per bidang
        Map<String, Integer> countsByBidang = new LinkedHashMap<>();
        Map<String, String> bidangNames = new LinkedHashMap<>();
        
        for (MstBidang bidang : allBidang) {
            countsByBidang.put(bidang.getKodeBidang(), 0);
            bidangNames.put(bidang.getKodeBidang(), bidang.getNamaBidang());
        }

        for (PksiDocument doc : approvedDocuments) {
            String bidangKode = resolveBidangKode(doc.getPicSatker(), skpaMap);
            if (bidangKode != null && countsByBidang.containsKey(bidangKode)) {
                countsByBidang.merge(bidangKode, 1, Integer::sum);
            }
        }

        // Build result list (only bidang with count > 0)
        List<BidangStat> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : countsByBidang.entrySet()) {
            if (entry.getValue() > 0) {
                result.add(BidangStat.builder()
                        .bidangKode(entry.getKey())
                        .bidangNama(bidangNames.get(entry.getKey()))
                        .count(entry.getValue())
                        .build());
            }
        }

        return result;
    }

    private List<PksiListItem> buildPksiList(
            List<PksiDocument> documents, 
            Map<UUID, MstSkpa> skpaMap,
            Map<UUID, String> historicalProgress) {
        
        return documents.stream()
                .map(doc -> {
                    String bidangNama = resolveBidangNama(doc.getPicSatker(), skpaMap);
                    String inisiatifNomor = null;
                    String inisiatifNama = null;
                    
                    if (doc.getInisiatif() != null) {
                        Hibernate.initialize(doc.getInisiatif());
                        inisiatifNomor = doc.getInisiatif().getNomorInisiatif();
                        inisiatifNama = doc.getInisiatif().getNamaInisiatif();
                    }

                    String progress = historicalProgress.getOrDefault(doc.getId(), doc.getProgress());

                    return PksiListItem.builder()
                            .id(doc.getId())
                            .namaPksi(doc.getNamaPksi())
                            .inisiatifNomor(inisiatifNomor)
                            .inisiatifNama(inisiatifNama)
                            .status(doc.getStatus() != null ? doc.getStatus().name() : null)
                            .progress(progress)
                            .bidangNama(bidangNama)
                            .tahap7Awal(doc.getTargetGoLive() != null ? doc.getTargetGoLive().toString() : null)
                            .tahap7Akhir(doc.getTargetGoLive() != null ? doc.getTargetGoLive().toString() : null)
                            .isMultiyear(isMultiyear(doc))
                            .inhouseOutsource(doc.getInhouseOutsource())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private List<MonthlyProgressTrend> calculateMonthlyTrend(List<PksiDocument> approvedDocuments, int year) {
        List<MonthlyProgressTrend> trend = new ArrayList<>();
        
        for (int month = 1; month <= 12; month++) {
            LocalDateTime monthEnd = YearMonth.of(year, month).atEndOfMonth().atTime(23, 59, 59);
            
            // Filter documents that existed at this month
            List<PksiDocument> documentsAtMonth = approvedDocuments.stream()
                    .filter(doc -> doc.getCreatedAt() != null && doc.getCreatedAt().isBefore(monthEnd))
                    .collect(Collectors.toList());
            
            Map<UUID, String> monthProgress = getHistoricalProgressState(documentsAtMonth, monthEnd);
            
            int early = 0, dev = 0, test = 0, complete = 0;
            
            for (PksiDocument doc : documentsAtMonth) {
                String progress = monthProgress.get(doc.getId());
                if (progress == null) continue;
                
                if (EARLY_STAGE.contains(progress)) early++;
                else if (DEV_STAGE.contains(progress)) dev++;
                else if (TEST_STAGE.contains(progress)) test++;
                else if (DEPLOY_STAGE.contains(progress)) complete++;
            }
            
            trend.add(MonthlyProgressTrend.builder()
                    .month(month)
                    .monthLabel(MONTH_NAMES[month].substring(0, 3))
                    .earlyStage(early)
                    .developmentStage(dev)
                    .testingStage(test)
                    .completed(complete)
                    .build());
        }
        
        return trend;
    }

    private String resolveBidangKode(String picSatker, Map<UUID, MstSkpa> skpaMap) {
        if (picSatker == null || picSatker.trim().isEmpty()) return null;

        String[] skpaIds = picSatker.split(",");
        for (String skpaIdStr : skpaIds) {
            try {
                UUID skpaId = UUID.fromString(skpaIdStr.trim());
                MstSkpa skpa = skpaMap.get(skpaId);
                if (skpa != null && skpa.getBidang() != null) {
                    return skpa.getBidang().getKodeBidang();
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID in pic_satker: {}", skpaIdStr);
            }
        }
        return null;
    }

    private String resolveBidangNama(String picSatker, Map<UUID, MstSkpa> skpaMap) {
        if (picSatker == null || picSatker.trim().isEmpty()) return null;

        String[] skpaIds = picSatker.split(",");
        for (String skpaIdStr : skpaIds) {
            try {
                UUID skpaId = UUID.fromString(skpaIdStr.trim());
                MstSkpa skpa = skpaMap.get(skpaId);
                if (skpa != null && skpa.getBidang() != null) {
                    return skpa.getBidang().getNamaBidang();
                }
            } catch (IllegalArgumentException e) {
                log.debug("Invalid UUID in pic_satker: {}", skpaIdStr);
            }
        }
        return null;
    }
}
