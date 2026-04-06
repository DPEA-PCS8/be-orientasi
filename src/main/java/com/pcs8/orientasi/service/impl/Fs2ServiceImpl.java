package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.Fs2DocumentRequest;
import com.pcs8.orientasi.domain.dto.response.Fs2DocumentResponse;
import com.pcs8.orientasi.domain.entity.Fs2Document;
import com.pcs8.orientasi.domain.entity.MstAplikasi;
import com.pcs8.orientasi.domain.entity.MstBidang;
import com.pcs8.orientasi.domain.entity.MstSkpa;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.Fs2DocumentRepository;
import com.pcs8.orientasi.repository.MstAplikasiRepository;
import com.pcs8.orientasi.repository.MstBidangRepository;
import com.pcs8.orientasi.repository.MstSkpaRepository;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.service.AuditService;
import com.pcs8.orientasi.service.Fs2ChangelogService;
import com.pcs8.orientasi.service.Fs2Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class Fs2ServiceImpl implements Fs2Service {

    private static final Logger log = LoggerFactory.getLogger(Fs2ServiceImpl.class);
    private static final String ENTITY_NAME = "Fs2Document";
    private static final String NOT_FOUND_WITH_ID = " not found with id: ";

    private final Fs2DocumentRepository fs2Repository;
    private final MstAplikasiRepository aplikasiRepository;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final MstUserRepository userRepository;
    private final AuditService auditService;
    private final UserContext userContext;
    private final Fs2ChangelogService fs2ChangelogService;

    @Override
    @Transactional
    public Fs2DocumentResponse create(Fs2DocumentRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        Fs2Document document = Fs2Document.builder()
                .userId(userId)
                .userName(username)
                .tanggalPengajuan(request.getTanggalPengajuan() != null ? request.getTanggalPengajuan() : LocalDate.now())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                // New form fields
                .deskripsiPengubahan(request.getDeskripsiPengubahan())
                .alasanPengubahan(request.getAlasanPengubahan())
                .statusTahapan(request.getStatusTahapan())
                .urgensi(request.getUrgensi())
                .kriteria1(Boolean.TRUE.equals(request.getKriteria1()))
                .kriteria2(Boolean.TRUE.equals(request.getKriteria2()))
                .kriteria3(Boolean.TRUE.equals(request.getKriteria3()))
                .kriteria4(Boolean.TRUE.equals(request.getKriteria4()))
                .aspekSistemAda(request.getAspekSistemAda())
                .aspekSistemTerkait(request.getAspekSistemTerkait())
                .aspekAlurKerja(request.getAspekAlurKerja())
                .aspekStrukturOrganisasi(request.getAspekStrukturOrganisasi())
                .dokT01Sebelum(request.getDokT01Sebelum())
                .dokT01Sesudah(request.getDokT01Sesudah())
                .dokT11Sebelum(request.getDokT11Sebelum())
                .dokT11Sesudah(request.getDokT11Sesudah())
                .penggunaSebelum(request.getPenggunaSebelum())
                .penggunaSesudah(request.getPenggunaSesudah())
                .aksesBersamaanSebelum(request.getAksesBersamaanSebelum())
                .aksesBersamaanSesudah(request.getAksesBersamaanSesudah())
                .pertumbuhanDataSebelum(request.getPertumbuhanDataSebelum())
                .pertumbuhanDataSesudah(request.getPertumbuhanDataSesudah())
                .targetPengujian(request.getTargetPengujian())
                .targetDeployment(request.getTargetDeployment())
                .targetGoLive(request.getTargetGoLive())
                .pernyataan1(Boolean.TRUE.equals(request.getPernyataan1()))
                .pernyataan2(Boolean.TRUE.equals(request.getPernyataan2()))
                // F.S.2 Disetujui fields
                .progres(request.getProgres())
                .fasePengajuan(request.getFasePengajuan())
                .iku(request.getIku())
                .mekanisme(request.getMekanisme())
                .pelaksanaan(request.getPelaksanaan())
                .tahun(request.getTahun())
                .tahunMulai(request.getTahunMulai())
                .tahunSelesai(request.getTahunSelesai())
                .dokumenPath(request.getDokumenPath())
                .build();

        setDocumentRelations(document, request);

        Fs2Document saved = fs2Repository.save(document);
        log.info("F.S.2 Document created: {}", saved.getId());

        Fs2DocumentResponse response = mapToResponse(saved);
        auditService.logCreate(ENTITY_NAME, saved.getId(), response, userId, username);

        return response;
    }

    @Override
    public Fs2DocumentResponse getById(UUID id) {
        Fs2Document document = fs2Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + NOT_FOUND_WITH_ID + id));
        return mapToResponse(document);
    }

    @Override
    public List<Fs2DocumentResponse> getAll() {
        return fs2Repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    public Page<Fs2DocumentResponse> search(String search, UUID bidangId, UUID skpaId, String status, Integer year, Pageable pageable, String userDepartment, boolean canSeeAll) {
        log.info("Searching F.S.2 documents - canSeeAll: {}, userDepartment: '{}', year: {}", canSeeAll, userDepartment, year);
        
        // Admin/Pengembang can see all documents
        if (canSeeAll) {
            log.info("User can see all - fetching all F.S.2 documents with year filter: {}", year);
            return fs2Repository.searchFs2DocumentsWithYear(search, bidangId, skpaId, status, year, pageable)
                    .map(this::mapToResponse);
        }
        
        // SKPA users: if department is empty, return empty result (security)
        if (userDepartment == null || userDepartment.trim().isEmpty()) {
            log.warn("SKPA user has no department set - returning empty result for security");
            return Page.empty(pageable);
        }
        
        // SKPA users only see documents where SKPA kode matches their department
        log.info("User is SKPA - filtering F.S.2 by department: '{}' and year: {}", userDepartment, year);
        
        // Find SKPA UUID for the user's department
        Optional<MstSkpa> userSkpa = skpaRepository.findByKodeSkpa(userDepartment.trim().toUpperCase());
        if (userSkpa.isPresent()) {
            log.info("Found SKPA for department '{}': UUID = {}", userDepartment, userSkpa.get().getId());
            return fs2Repository.searchFs2DocumentsByDepartmentWithYear(search, bidangId, status, userDepartment.trim(), year, pageable)
                    .map(this::mapToResponse);
        } else {
            log.warn("No SKPA found for department '{}' - user will see no F.S.2", userDepartment);
            return Page.empty(pageable);
        }
    }

    @Override
    public List<Fs2DocumentResponse> searchList(String search, UUID bidangId, UUID skpaId, String status, String userDepartment, boolean canSeeAll) {
        log.info("Searching F.S.2 list - canSeeAll: {}, userDepartment: '{}'", canSeeAll, userDepartment);
        
        // Admin/Pengembang can see all documents
        if (canSeeAll) {
            return fs2Repository.searchFs2DocumentsList(search, bidangId, skpaId, status)
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        }
        
        // SKPA users: if department is empty, return empty result (security)
        if (userDepartment == null || userDepartment.trim().isEmpty()) {
            log.warn("SKPA user has no department set - returning empty list for security");
            return Collections.emptyList();
        }
        
        // SKPA users only see documents where SKPA kode matches their department
        Optional<MstSkpa> userSkpa = skpaRepository.findByKodeSkpa(userDepartment.trim().toUpperCase());
        if (userSkpa.isPresent()) {
            return fs2Repository.searchFs2DocumentsListByDepartment(search, bidangId, status, userDepartment.trim())
                    .stream()
                    .map(this::mapToResponse)
                    .toList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public Page<Fs2DocumentResponse> searchApproved(
            com.pcs8.orientasi.domain.dto.request.Fs2ApprovedSearchFilter filter,
            Pageable pageable,
            String userDepartment,
            boolean canSeeAll) {
        log.info("Searching approved F.S.2 - canSeeAll: {}, userDepartment: '{}'", canSeeAll, userDepartment);
        
        // Admin/Pengembang can see all documents
        if (canSeeAll) {
            return fs2Repository.searchApprovedFs2Documents(filter, pageable)
                    .map(this::mapToResponse);
        }
        
        // SKPA users: if department is empty, return empty result (security)
        if (userDepartment == null || userDepartment.trim().isEmpty()) {
            log.warn("SKPA user has no department set - returning empty approved list for security");
            return Page.empty(pageable);
        }
        
        // SKPA users only see approved documents where SKPA kode matches their department
        Optional<MstSkpa> userSkpa = skpaRepository.findByKodeSkpa(userDepartment.trim().toUpperCase());
        if (userSkpa.isPresent()) {
            return fs2Repository.searchApprovedFs2DocumentsByDepartment(filter, userDepartment.trim(), pageable)
                    .map(this::mapToResponse);
        } else {
            return Page.empty(pageable);
        }
    }

    @Override
    @Transactional
    public Fs2DocumentResponse update(UUID id, Fs2DocumentRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        Fs2Document document = fs2Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + NOT_FOUND_WITH_ID + id));

        // Create snapshot of old document for changelog tracking
        Fs2Document oldDocument = createSnapshot(document);
        Fs2DocumentResponse oldValue = mapToResponse(document);

        // Update fields
        if (request.getTanggalPengajuan() != null) {
            document.setTanggalPengajuan(request.getTanggalPengajuan());
        }
        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }
        
        // Update form fields - only update if not null to prevent data loss
        if (request.getDeskripsiPengubahan() != null) document.setDeskripsiPengubahan(request.getDeskripsiPengubahan());
        if (request.getAlasanPengubahan() != null) document.setAlasanPengubahan(request.getAlasanPengubahan());
        if (request.getStatusTahapan() != null) document.setStatusTahapan(request.getStatusTahapan());
        if (request.getUrgensi() != null) document.setUrgensi(request.getUrgensi());
        if (request.getKriteria1() != null) document.setKriteria1(request.getKriteria1());
        if (request.getKriteria2() != null) document.setKriteria2(request.getKriteria2());
        if (request.getKriteria3() != null) document.setKriteria3(request.getKriteria3());
        if (request.getKriteria4() != null) document.setKriteria4(request.getKriteria4());
        if (request.getAspekSistemAda() != null) document.setAspekSistemAda(request.getAspekSistemAda());
        if (request.getAspekSistemTerkait() != null) document.setAspekSistemTerkait(request.getAspekSistemTerkait());
        if (request.getAspekAlurKerja() != null) document.setAspekAlurKerja(request.getAspekAlurKerja());
        if (request.getAspekStrukturOrganisasi() != null) document.setAspekStrukturOrganisasi(request.getAspekStrukturOrganisasi());
        if (request.getDokT01Sebelum() != null) document.setDokT01Sebelum(request.getDokT01Sebelum());
        if (request.getDokT01Sesudah() != null) document.setDokT01Sesudah(request.getDokT01Sesudah());
        if (request.getDokT11Sebelum() != null) document.setDokT11Sebelum(request.getDokT11Sebelum());
        if (request.getDokT11Sesudah() != null) document.setDokT11Sesudah(request.getDokT11Sesudah());
        if (request.getPenggunaSebelum() != null) document.setPenggunaSebelum(request.getPenggunaSebelum());
        if (request.getPenggunaSesudah() != null) document.setPenggunaSesudah(request.getPenggunaSesudah());
        if (request.getAksesBersamaanSebelum() != null) document.setAksesBersamaanSebelum(request.getAksesBersamaanSebelum());
        if (request.getAksesBersamaanSesudah() != null) document.setAksesBersamaanSesudah(request.getAksesBersamaanSesudah());
        if (request.getPertumbuhanDataSebelum() != null) document.setPertumbuhanDataSebelum(request.getPertumbuhanDataSebelum());
        if (request.getPertumbuhanDataSesudah() != null) document.setPertumbuhanDataSesudah(request.getPertumbuhanDataSesudah());
        if (request.getTargetPengujian() != null) document.setTargetPengujian(request.getTargetPengujian());
        if (request.getTargetDeployment() != null) document.setTargetDeployment(request.getTargetDeployment());
        if (request.getTargetGoLive() != null) document.setTargetGoLive(request.getTargetGoLive());
        if (request.getPernyataan1() != null) document.setPernyataan1(request.getPernyataan1());
        if (request.getPernyataan2() != null) document.setPernyataan2(request.getPernyataan2());
        
        // F.S.2 Disetujui fields - only update if not null
        if (request.getProgres() != null) document.setProgres(request.getProgres());
        if (request.getFasePengajuan() != null) document.setFasePengajuan(request.getFasePengajuan());
        if (request.getIku() != null) document.setIku(request.getIku());
        if (request.getMekanisme() != null) document.setMekanisme(request.getMekanisme());
        if (request.getPelaksanaan() != null) document.setPelaksanaan(request.getPelaksanaan());
        if (request.getTahun() != null) document.setTahun(request.getTahun());
        if (request.getTahunMulai() != null) document.setTahunMulai(request.getTahunMulai());
        if (request.getTahunSelesai() != null) document.setTahunSelesai(request.getTahunSelesai());
        if (request.getDokumenPath() != null) document.setDokumenPath(request.getDokumenPath());

        // Monitoring Fields - Dokumen Pengajuan F.S.2 - only update if not null
        if (request.getNomorNd() != null) document.setNomorNd(request.getNomorNd());
        if (request.getTanggalNd() != null) document.setTanggalNd(request.getTanggalNd());
        if (request.getBerkasNd() != null) document.setBerkasNd(request.getBerkasNd());
        if (request.getBerkasFs2() != null) document.setBerkasFs2(request.getBerkasFs2());

        // Monitoring Fields - CD Prinsip - only update if not null
        if (request.getNomorCd() != null) document.setNomorCd(request.getNomorCd());
        if (request.getTanggalCd() != null) document.setTanggalCd(request.getTanggalCd());
        if (request.getBerkasCd() != null) document.setBerkasCd(request.getBerkasCd());
        if (request.getBerkasFs2a() != null) document.setBerkasFs2a(request.getBerkasFs2a());
        if (request.getBerkasFs2b() != null) document.setBerkasFs2b(request.getBerkasFs2b());

        // Monitoring Fields - Pengujian - only update if not null
        if (request.getRealisasiPengujian() != null) document.setRealisasiPengujian(request.getRealisasiPengujian());
        if (request.getBerkasF45() != null) document.setBerkasF45(request.getBerkasF45());
        if (request.getBerkasF46() != null) document.setBerkasF46(request.getBerkasF46());

        // Monitoring Fields - Deployment - only update if not null
        if (request.getRealisasiDeployment() != null) document.setRealisasiDeployment(request.getRealisasiDeployment());
        if (request.getBerkasNdBaDeployment() != null) document.setBerkasNdBaDeployment(request.getBerkasNdBaDeployment());

        // Monitoring Fields - Keterangan - only update if not null
        if (request.getKeterangan() != null) document.setKeterangan(request.getKeterangan());

        setDocumentRelations(document, request);

        Fs2Document saved = fs2Repository.save(document);
        log.info("F.S.2 Document updated: {}", saved.getId());

        // Track changes for changelog
        MstUser updatedByUser = userRepository.findById(userId).orElse(null);
        fs2ChangelogService.trackChanges(saved, oldDocument, updatedByUser);

        Fs2DocumentResponse response = mapToResponse(saved);
        auditService.logUpdate(ENTITY_NAME, saved.getId(), oldValue, response, userId, username);

        return response;
    }

    @Override
    @Transactional
    public Fs2DocumentResponse updateStatus(UUID id, String status) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        Fs2Document document = fs2Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + NOT_FOUND_WITH_ID + id));

        // Create snapshot for changelog
        Fs2Document oldDocument = createSnapshot(document);
        Fs2DocumentResponse oldValue = mapToResponse(document);
        document.setStatus(status);

        Fs2Document saved = fs2Repository.save(document);

        // Track changes for changelog
        MstUser updatedByUser = userRepository.findById(userId).orElse(null);
        fs2ChangelogService.trackChanges(saved, oldDocument, updatedByUser);

        Fs2DocumentResponse response = mapToResponse(saved);
        auditService.logUpdate(ENTITY_NAME, saved.getId(), oldValue, response, userId, username);

        return response;
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        Fs2Document document = fs2Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + NOT_FOUND_WITH_ID + id));

        Fs2DocumentResponse oldValue = mapToResponse(document);
        fs2Repository.delete(document);
        log.info("F.S.2 Document deleted: {}", id);

        auditService.logDelete(ENTITY_NAME, id, oldValue, userId, username);
    }

    /**
     * Create a snapshot of the document for changelog comparison
     */
    private Fs2Document createSnapshot(Fs2Document document) {
        return Fs2Document.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .userName(document.getUserName())
                .tanggalPengajuan(document.getTanggalPengajuan())
                .status(document.getStatus())
                .deskripsiPengubahan(document.getDeskripsiPengubahan())
                .alasanPengubahan(document.getAlasanPengubahan())
                .statusTahapan(document.getStatusTahapan())
                .urgensi(document.getUrgensi())
                .kriteria1(document.getKriteria1())
                .kriteria2(document.getKriteria2())
                .kriteria3(document.getKriteria3())
                .kriteria4(document.getKriteria4())
                .aspekSistemAda(document.getAspekSistemAda())
                .aspekSistemTerkait(document.getAspekSistemTerkait())
                .aspekAlurKerja(document.getAspekAlurKerja())
                .aspekStrukturOrganisasi(document.getAspekStrukturOrganisasi())
                .dokT01Sebelum(document.getDokT01Sebelum())
                .dokT01Sesudah(document.getDokT01Sesudah())
                .dokT11Sebelum(document.getDokT11Sebelum())
                .dokT11Sesudah(document.getDokT11Sesudah())
                .penggunaSebelum(document.getPenggunaSebelum())
                .penggunaSesudah(document.getPenggunaSesudah())
                .aksesBersamaanSebelum(document.getAksesBersamaanSebelum())
                .aksesBersamaanSesudah(document.getAksesBersamaanSesudah())
                .pertumbuhanDataSebelum(document.getPertumbuhanDataSebelum())
                .pertumbuhanDataSesudah(document.getPertumbuhanDataSesudah())
                .targetPengujian(document.getTargetPengujian())
                .targetDeployment(document.getTargetDeployment())
                .targetGoLive(document.getTargetGoLive())
                .pernyataan1(document.getPernyataan1())
                .pernyataan2(document.getPernyataan2())
                .progres(document.getProgres())
                .fasePengajuan(document.getFasePengajuan())
                .iku(document.getIku())
                .mekanisme(document.getMekanisme())
                .pelaksanaan(document.getPelaksanaan())
                .tahun(document.getTahun())
                .tahunMulai(document.getTahunMulai())
                .tahunSelesai(document.getTahunSelesai())
                .picId(document.getPicId())
                .picName(document.getPicName())
                .dokumenPath(document.getDokumenPath())
                .nomorNd(document.getNomorNd())
                .tanggalNd(document.getTanggalNd())
                .berkasNd(document.getBerkasNd())
                .berkasFs2(document.getBerkasFs2())
                .nomorCd(document.getNomorCd())
                .tanggalCd(document.getTanggalCd())
                .berkasCd(document.getBerkasCd())
                .berkasFs2a(document.getBerkasFs2a())
                .berkasFs2b(document.getBerkasFs2b())
                .realisasiPengujian(document.getRealisasiPengujian())
                .berkasF45(document.getBerkasF45())
                .berkasF46(document.getBerkasF46())
                .realisasiDeployment(document.getRealisasiDeployment())
                .berkasNdBaDeployment(document.getBerkasNdBaDeployment())
                .keterangan(document.getKeterangan())
                .build();
    }

    /**
     * Set document relations (Aplikasi, Bidang, SKPA, PIC) from request
     */
    private void setDocumentRelations(Fs2Document document, Fs2DocumentRequest request) {
        if (request.getAplikasiId() != null) {
            MstAplikasi aplikasi = aplikasiRepository.findById(request.getAplikasiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi" + NOT_FOUND_WITH_ID + request.getAplikasiId()));
            document.setAplikasi(aplikasi);
        }

        if (request.getBidangId() != null) {
            MstBidang bidang = bidangRepository.findById(request.getBidangId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bidang" + NOT_FOUND_WITH_ID + request.getBidangId()));
            document.setBidang(bidang);
        }

        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA" + NOT_FOUND_WITH_ID + request.getSkpaId()));
            document.setSkpa(skpa);
        }

        if (request.getPicId() != null) {
            MstUser pic = userRepository.findById(request.getPicId())
                    .orElseThrow(() -> new ResourceNotFoundException("User" + NOT_FOUND_WITH_ID + request.getPicId()));
            document.setPicId(pic.getUuid());
            document.setPicName(pic.getFullName());
        }
    }

    private Fs2DocumentResponse mapToResponse(Fs2Document document) {
        Fs2DocumentResponse.Fs2DocumentResponseBuilder builder = Fs2DocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .userName(document.getUserName())
                .tanggalPengajuan(document.getTanggalPengajuan())
                .status(document.getStatus())
                // New form fields
                .deskripsiPengubahan(document.getDeskripsiPengubahan())
                .alasanPengubahan(document.getAlasanPengubahan())
                .statusTahapan(document.getStatusTahapan())
                .urgensi(document.getUrgensi())
                .kriteria1(document.getKriteria1())
                .kriteria2(document.getKriteria2())
                .kriteria3(document.getKriteria3())
                .kriteria4(document.getKriteria4())
                .aspekSistemAda(document.getAspekSistemAda())
                .aspekSistemTerkait(document.getAspekSistemTerkait())
                .aspekAlurKerja(document.getAspekAlurKerja())
                .aspekStrukturOrganisasi(document.getAspekStrukturOrganisasi())
                .dokT01Sebelum(document.getDokT01Sebelum())
                .dokT01Sesudah(document.getDokT01Sesudah())
                .dokT11Sebelum(document.getDokT11Sebelum())
                .dokT11Sesudah(document.getDokT11Sesudah())
                .penggunaSebelum(document.getPenggunaSebelum())
                .penggunaSesudah(document.getPenggunaSesudah())
                .aksesBersamaanSebelum(document.getAksesBersamaanSebelum())
                .aksesBersamaanSesudah(document.getAksesBersamaanSesudah())
                .pertumbuhanDataSebelum(document.getPertumbuhanDataSebelum())
                .pertumbuhanDataSesudah(document.getPertumbuhanDataSesudah())
                .targetPengujian(document.getTargetPengujian())
                .targetDeployment(document.getTargetDeployment())
                .targetGoLive(document.getTargetGoLive())
                .pernyataan1(document.getPernyataan1())
                .pernyataan2(document.getPernyataan2())
                // F.S.2 Disetujui fields
                .progres(document.getProgres())
                .fasePengajuan(document.getFasePengajuan())
                .iku(document.getIku())
                .mekanisme(document.getMekanisme())
                .pelaksanaan(document.getPelaksanaan())
                .tahun(document.getTahun())
                .tahunMulai(document.getTahunMulai())
                .tahunSelesai(document.getTahunSelesai())
                .picId(document.getPicId())
                .picName(document.getPicName())
                .dokumenPath(document.getDokumenPath())
                // Monitoring Fields - Dokumen Pengajuan F.S.2
                .nomorNd(document.getNomorNd())
                .tanggalNd(document.getTanggalNd())
                .berkasNd(document.getBerkasNd())
                .berkasFs2(document.getBerkasFs2())
                // Monitoring Fields - CD Prinsip
                .nomorCd(document.getNomorCd())
                .tanggalCd(document.getTanggalCd())
                .berkasCd(document.getBerkasCd())
                .berkasFs2a(document.getBerkasFs2a())
                .berkasFs2b(document.getBerkasFs2b())
                // Monitoring Fields - Pengujian
                .realisasiPengujian(document.getRealisasiPengujian())
                .berkasF45(document.getBerkasF45())
                .berkasF46(document.getBerkasF46())
                // Monitoring Fields - Deployment
                .realisasiDeployment(document.getRealisasiDeployment())
                .berkasNdBaDeployment(document.getBerkasNdBaDeployment())
                // Monitoring Fields - Keterangan
                .keterangan(document.getKeterangan())
                .createdAt(document.getCreatedAt())
                .updatedAt(document.getUpdatedAt());

        if (document.getAplikasi() != null) {
            builder.aplikasiId(document.getAplikasi().getId())
                    .namaAplikasi(document.getAplikasi().getNamaAplikasi())
                    .kodeAplikasi(document.getAplikasi().getKodeAplikasi());
        }

        if (document.getBidang() != null) {
            builder.bidangId(document.getBidang().getId())
                    .namaBidang(document.getBidang().getNamaBidang());
        }

        if (document.getSkpa() != null) {
            builder.skpaId(document.getSkpa().getId())
                    .namaSkpa(document.getSkpa().getNamaSkpa())
                    .kodeSkpa(document.getSkpa().getKodeSkpa());
        }

        return builder.build();
    }
}
