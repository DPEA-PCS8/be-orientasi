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
import com.pcs8.orientasi.service.Fs2Service;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Fs2ServiceImpl implements Fs2Service {

    private static final Logger log = LoggerFactory.getLogger(Fs2ServiceImpl.class);
    private static final String ENTITY_NAME = "Fs2Document";

    private final Fs2DocumentRepository fs2Repository;
    private final MstAplikasiRepository aplikasiRepository;
    private final MstBidangRepository bidangRepository;
    private final MstSkpaRepository skpaRepository;
    private final MstUserRepository userRepository;
    private final AuditService auditService;
    private final UserContext userContext;

    @Override
    @Transactional
    public Fs2DocumentResponse create(Fs2DocumentRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        Fs2Document document = Fs2Document.builder()
                .userId(userId)
                .userName(username)
                .namaFs2(request.getNamaFs2().trim())
                .tanggalPengajuan(request.getTanggalPengajuan() != null ? request.getTanggalPengajuan() : LocalDate.now())
                .status(request.getStatus() != null ? request.getStatus() : "PENDING")
                // New form fields
                .deskripsiPengubahan(request.getDeskripsiPengubahan())
                .alasanPengubahan(request.getAlasanPengubahan())
                .statusTahapan(request.getStatusTahapan())
                .urgensi(request.getUrgensi())
                .kriteria1(request.getKriteria1() != null ? request.getKriteria1() : false)
                .kriteria2(request.getKriteria2() != null ? request.getKriteria2() : false)
                .kriteria3(request.getKriteria3() != null ? request.getKriteria3() : false)
                .kriteria4(request.getKriteria4() != null ? request.getKriteria4() : false)
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
                .pernyataan1(request.getPernyataan1() != null ? request.getPernyataan1() : false)
                .pernyataan2(request.getPernyataan2() != null ? request.getPernyataan2() : false)
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

        // Set relations
        if (request.getAplikasiId() != null) {
            MstAplikasi aplikasi = aplikasiRepository.findById(request.getAplikasiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi not found with id: " + request.getAplikasiId()));
            document.setAplikasi(aplikasi);
        }

        if (request.getBidangId() != null) {
            MstBidang bidang = bidangRepository.findById(request.getBidangId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bidang not found with id: " + request.getBidangId()));
            document.setBidang(bidang);
        }

        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA not found with id: " + request.getSkpaId()));
            document.setSkpa(skpa);
        }

        if (request.getPicId() != null) {
            MstUser pic = userRepository.findById(request.getPicId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getPicId()));
            document.setPicId(pic.getUuid());
            document.setPicName(pic.getFullName());
        }

        Fs2Document saved = fs2Repository.save(document);
        log.info("F.S.2 Document created: {}", saved.getNamaFs2());

        Fs2DocumentResponse response = mapToResponse(saved);
        auditService.logCreate(ENTITY_NAME, saved.getId(), response, userId, username);

        return response;
    }

    @Override
    public Fs2DocumentResponse getById(UUID id) {
        Fs2Document document = fs2Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + " not found with id: " + id));
        return mapToResponse(document);
    }

    @Override
    public List<Fs2DocumentResponse> getAll() {
        return fs2Repository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Fs2DocumentResponse> search(String search, UUID bidangId, UUID skpaId, String status, Pageable pageable) {
        return fs2Repository.searchFs2Documents(search, bidangId, skpaId, status, pageable)
                .map(this::mapToResponse);
    }

    @Override
    public List<Fs2DocumentResponse> searchList(String search, UUID bidangId, UUID skpaId, String status) {
        return fs2Repository.searchFs2DocumentsList(search, bidangId, skpaId, status)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public Page<Fs2DocumentResponse> searchApproved(
            String search, UUID bidangId, UUID skpaId,
            String progres, String fasePengajuan,
            String mekanisme, String pelaksanaan,
            Pageable pageable) {
        return fs2Repository.searchApprovedFs2Documents(
                search, bidangId, skpaId, progres, fasePengajuan, mekanisme, pelaksanaan, pageable
        ).map(this::mapToResponse);
    }

    @Override
    @Transactional
    public Fs2DocumentResponse update(UUID id, Fs2DocumentRequest request) {
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();

        Fs2Document document = fs2Repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + " not found with id: " + id));

        Fs2DocumentResponse oldValue = mapToResponse(document);

        // Update fields
        document.setNamaFs2(request.getNamaFs2().trim());
        if (request.getTanggalPengajuan() != null) {
            document.setTanggalPengajuan(request.getTanggalPengajuan());
        }
        if (request.getStatus() != null) {
            document.setStatus(request.getStatus());
        }
        
        // Update new form fields
        document.setDeskripsiPengubahan(request.getDeskripsiPengubahan());
        document.setAlasanPengubahan(request.getAlasanPengubahan());
        document.setStatusTahapan(request.getStatusTahapan());
        document.setUrgensi(request.getUrgensi());
        if (request.getKriteria1() != null) document.setKriteria1(request.getKriteria1());
        if (request.getKriteria2() != null) document.setKriteria2(request.getKriteria2());
        if (request.getKriteria3() != null) document.setKriteria3(request.getKriteria3());
        if (request.getKriteria4() != null) document.setKriteria4(request.getKriteria4());
        document.setAspekSistemAda(request.getAspekSistemAda());
        document.setAspekSistemTerkait(request.getAspekSistemTerkait());
        document.setAspekAlurKerja(request.getAspekAlurKerja());
        document.setAspekStrukturOrganisasi(request.getAspekStrukturOrganisasi());
        document.setDokT01Sebelum(request.getDokT01Sebelum());
        document.setDokT01Sesudah(request.getDokT01Sesudah());
        document.setDokT11Sebelum(request.getDokT11Sebelum());
        document.setDokT11Sesudah(request.getDokT11Sesudah());
        document.setPenggunaSebelum(request.getPenggunaSebelum());
        document.setPenggunaSesudah(request.getPenggunaSesudah());
        document.setAksesBersamaanSebelum(request.getAksesBersamaanSebelum());
        document.setAksesBersamaanSesudah(request.getAksesBersamaanSesudah());
        document.setPertumbuhanDataSebelum(request.getPertumbuhanDataSebelum());
        document.setPertumbuhanDataSesudah(request.getPertumbuhanDataSesudah());
        document.setTargetPengujian(request.getTargetPengujian());
        document.setTargetDeployment(request.getTargetDeployment());
        document.setTargetGoLive(request.getTargetGoLive());
        if (request.getPernyataan1() != null) document.setPernyataan1(request.getPernyataan1());
        if (request.getPernyataan2() != null) document.setPernyataan2(request.getPernyataan2());
        
        // F.S.2 Disetujui fields
        document.setProgres(request.getProgres());
        document.setFasePengajuan(request.getFasePengajuan());
        document.setIku(request.getIku());
        document.setMekanisme(request.getMekanisme());
        document.setPelaksanaan(request.getPelaksanaan());
        document.setTahun(request.getTahun());
        document.setTahunMulai(request.getTahunMulai());
        document.setTahunSelesai(request.getTahunSelesai());
        document.setDokumenPath(request.getDokumenPath());

        // Update relations
        if (request.getAplikasiId() != null) {
            MstAplikasi aplikasi = aplikasiRepository.findById(request.getAplikasiId())
                    .orElseThrow(() -> new ResourceNotFoundException("Aplikasi not found with id: " + request.getAplikasiId()));
            document.setAplikasi(aplikasi);
        }

        if (request.getBidangId() != null) {
            MstBidang bidang = bidangRepository.findById(request.getBidangId())
                    .orElseThrow(() -> new ResourceNotFoundException("Bidang not found with id: " + request.getBidangId()));
            document.setBidang(bidang);
        }

        if (request.getSkpaId() != null) {
            MstSkpa skpa = skpaRepository.findById(request.getSkpaId())
                    .orElseThrow(() -> new ResourceNotFoundException("SKPA not found with id: " + request.getSkpaId()));
            document.setSkpa(skpa);
        }

        if (request.getPicId() != null) {
            MstUser pic = userRepository.findById(request.getPicId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getPicId()));
            document.setPicId(pic.getUuid());
            document.setPicName(pic.getFullName());
        }

        Fs2Document saved = fs2Repository.save(document);
        log.info("F.S.2 Document updated: {}", saved.getNamaFs2());

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
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + " not found with id: " + id));

        Fs2DocumentResponse oldValue = mapToResponse(document);
        document.setStatus(status);

        Fs2Document saved = fs2Repository.save(document);
        log.info("F.S.2 Document status updated: {} -> {}", saved.getNamaFs2(), status);

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
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME + " not found with id: " + id));

        Fs2DocumentResponse oldValue = mapToResponse(document);
        fs2Repository.delete(document);
        log.info("F.S.2 Document deleted: {}", document.getNamaFs2());

        auditService.logDelete(ENTITY_NAME, id, oldValue, userId, username);
    }

    private Fs2DocumentResponse mapToResponse(Fs2Document document) {
        Fs2DocumentResponse.Fs2DocumentResponseBuilder builder = Fs2DocumentResponse.builder()
                .id(document.getId())
                .userId(document.getUserId())
                .userName(document.getUserName())
                .namaFs2(document.getNamaFs2())
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
