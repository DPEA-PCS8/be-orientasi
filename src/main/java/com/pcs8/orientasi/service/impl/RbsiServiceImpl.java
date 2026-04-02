package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.request.BatchKepProgressRequest;
import com.pcs8.orientasi.domain.dto.request.KepProgressRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiAnalyticsRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifItemRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiKepRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiProgramRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.BatchKepProgressResponse;
import com.pcs8.orientasi.domain.dto.response.InisiatifGroupResponse;
import com.pcs8.orientasi.domain.dto.response.KepProgressFullResponse;
import com.pcs8.orientasi.domain.dto.response.KepProgressResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiAnalyticsResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiHistoryResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiInisiatifResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiKepResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiMonitoringResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiProgramResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;
import com.pcs8.orientasi.domain.entity.InisiatifGroup;
import com.pcs8.orientasi.domain.entity.KepProgress;
import com.pcs8.orientasi.domain.entity.Rbsi;
import com.pcs8.orientasi.domain.entity.RbsiInisiatif;
import com.pcs8.orientasi.domain.entity.RbsiKep;
import com.pcs8.orientasi.domain.entity.RbsiProgram;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.InisiatifGroupRepository;
import com.pcs8.orientasi.repository.KepProgressRepository;
import com.pcs8.orientasi.repository.RbsiInisiatifRepository;
import com.pcs8.orientasi.repository.RbsiKepRepository;
import com.pcs8.orientasi.repository.RbsiProgramRepository;
import com.pcs8.orientasi.repository.RbsiRepository;
import com.pcs8.orientasi.service.AuditService;
import com.pcs8.orientasi.service.RbsiService;
import com.pcs8.orientasi.util.NomorComparator;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbsiServiceImpl implements RbsiService {

    private static final Logger log = LoggerFactory.getLogger(RbsiServiceImpl.class);
    private static final String ENTITY_NAME_RBSI = "RBSI";
    private static final String ENTITY_NAME_PROGRAM = "Program RBSI";
    private static final String ENTITY_NAME_INISIATIF = "Inisiatif RBSI";

    private final RbsiRepository rbsiRepository;
    private final RbsiProgramRepository programRepository;
    private final RbsiInisiatifRepository inisiatifRepository;
    private final InisiatifGroupRepository inisiatifGroupRepository;
    private final RbsiKepRepository kepRepository;
    private final KepProgressRepository kepProgressRepository;
    private final AuditService auditService;
    private final UserContext userContext;

    @Override
    @Transactional
    public RbsiResponse createRbsi(RbsiRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        if (rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new BadRequestException("RBSI dengan periode ini sudah ada");
        }

        Rbsi rbsi = Rbsi.builder()
                .periode(request.getPeriode())
                .build();

        Rbsi saved = rbsiRepository.save(rbsi);
        log.info("RBSI created: {}", saved.getId());
        
        // Audit log
        RbsiResponse response = mapToRbsiResponse(saved, null);
        auditService.logCreate(ENTITY_NAME_RBSI, saved.getId(), response, userId, username);
        
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiResponse getRbsi(UUID id, Integer tahun) {
        Rbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<RbsiProgramResponse> programs = null;

        if (tahun != null) {
            // If specific year requested, return only that year
            programs = getProgramsByRbsiAndTahun(rbsi.getId(), tahun);
        } else {
            // If no year specified, return programs from all years
            List<Integer> years = programRepository.findDistinctTahunByRbsiId(rbsi.getId());
            if (!years.isEmpty()) {
                programs = new ArrayList<>();
                for (Integer year : years) {
                    programs.addAll(getProgramsByRbsiAndTahun(rbsi.getId(), year));
                }
            }
        }

        return mapToRbsiResponse(rbsi, programs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RbsiResponse> getAllRbsi() {
        return rbsiRepository.findAllByOrderByPeriodeAsc().stream()
            .map(rbsi -> mapToRbsiResponse(rbsi, null))
            .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RbsiResponse updateRbsi(UUID id, RbsiRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        Rbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        // Capture old value untuk audit
        RbsiResponse oldValue = mapToRbsiResponse(rbsi, null);

        if (!rbsi.getPeriode().equals(request.getPeriode()) && rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new BadRequestException("RBSI dengan periode ini sudah ada");
        }

        rbsi.setPeriode(request.getPeriode());
        Rbsi saved = rbsiRepository.save(rbsi);
        log.info("RBSI updated: {}", saved.getId());
        
        // Audit log
        RbsiResponse newValue = mapToRbsiResponse(saved, null);
        auditService.logUpdate(ENTITY_NAME_RBSI, id, oldValue, newValue, userId, username);
        
        return newValue;
    }

    @Override
    @Transactional
    public void deleteRbsi(UUID id) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        Rbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));
        
        // Capture old value untuk audit sebelum delete
        RbsiResponse oldValue = mapToRbsiResponse(rbsi, null);
        
        rbsiRepository.delete(rbsi);
        log.info("RBSI deleted: {}", id);
        
        // Audit log
        auditService.logDelete(ENTITY_NAME_RBSI, id, oldValue, userId, username);
    }

    @Override
    @Transactional
    public RbsiProgramResponse createOrUpdateProgram(RbsiProgramRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        Rbsi rbsi = rbsiRepository.findById(request.getRbsiId())
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        RbsiProgram existingProgram = programRepository
                .findByRbsiIdAndTahunAndNomorProgramAndIsDeletedFalse(request.getRbsiId(), request.getTahun(), request.getNomorProgram())
                .orElse(null);
        
        boolean isNew = (existingProgram == null);
        RbsiProgramResponse oldValue = isNew ? null : mapToProgramResponse(existingProgram, false);
        
        RbsiProgram program = existingProgram != null ? existingProgram : RbsiProgram.builder()
                .rbsi(rbsi)
                .tahun(request.getTahun())
                .nomorProgram(request.getNomorProgram())
                .build();

        program.setNamaProgram(request.getNamaProgram());
        RbsiProgram savedProgram = programRepository.save(program);

        if (request.getInisiatifs() != null) {
            upsertInisiatifs(savedProgram, request.getInisiatifs());
        }

        log.info("Program saved: {}", savedProgram.getId());
        
        // Audit log
        RbsiProgramResponse response = mapToProgramResponse(savedProgram, true);
        if (isNew) {
            auditService.logCreate(ENTITY_NAME_PROGRAM, savedProgram.getId(), response, userId, username);
        } else {
            auditService.logUpdate(ENTITY_NAME_PROGRAM, savedProgram.getId(), oldValue, response, userId, username);
        }
        
        return response;
    }

    @Override
    @Transactional
    public RbsiProgramResponse updateProgram(UUID programId, RbsiProgramRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        RbsiProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));

        // Capture old value untuk audit
        RbsiProgramResponse oldValue = mapToProgramResponse(program, false);

        if (!program.getRbsi().getId().equals(request.getRbsiId())) {
            throw new BadRequestException("Rbsi id tidak sesuai dengan program");
        }

        if (!program.getNomorProgram().equals(request.getNomorProgram())
                || !program.getTahun().equals(request.getTahun())) {
            boolean exists = programRepository.existsByRbsiIdAndTahunAndNomorProgramAndIsDeletedFalse(
                    request.getRbsiId(), request.getTahun(), request.getNomorProgram()
            );
            if (exists) {
                throw new BadRequestException("Nomor program sudah digunakan di tahun tersebut");
            }
        }

        Integer previousTahun = program.getTahun();
        program.setTahun(request.getTahun());
        program.setNomorProgram(request.getNomorProgram());
        program.setNamaProgram(request.getNamaProgram());
        RbsiProgram savedProgram = programRepository.save(program);

        if (!previousTahun.equals(request.getTahun())) {
            List<RbsiInisiatif> inisiatifs = inisiatifRepository
                    .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(savedProgram.getId(), previousTahun);
            for (RbsiInisiatif inisiatif : inisiatifs) {
                inisiatif.setTahun(request.getTahun());
                inisiatifRepository.save(inisiatif);
            }
        }

        if (request.getInisiatifs() != null) {
            upsertInisiatifs(savedProgram, request.getInisiatifs());
        }

        log.info("Program updated: {}", savedProgram.getId());
        
        // Audit log
        RbsiProgramResponse newValue = mapToProgramResponse(savedProgram, true);
        auditService.logUpdate(ENTITY_NAME_PROGRAM, programId, oldValue, newValue, userId, username);
        
        return newValue;
    }

    @Override
    @Transactional
    public void deleteProgram(UUID programId) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        RbsiProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));
        
        // Check if already deleted
        if (Boolean.TRUE.equals(program.getIsDeleted())) {
            throw new BadRequestException("Program sudah dihapus sebelumnya");
        }
        
        // Capture old value untuk audit sebelum delete
        RbsiProgramResponse oldValue = mapToProgramResponse(program, false);
        
        // Soft delete all child inisiatifs
        List<RbsiInisiatif> inisiatifs = inisiatifRepository.findByProgramIdAndIsDeletedFalse(programId);
        for (RbsiInisiatif inisiatif : inisiatifs) {
            inisiatif.setIsDeleted(true);
            inisiatifRepository.save(inisiatif);
            log.info("Inisiatif soft deleted (cascade): {}", inisiatif.getId());
        }
        
        // Soft delete the program
        program.setIsDeleted(true);
        programRepository.save(program);
        log.info("Program soft deleted: {} with {} inisiatifs", programId, inisiatifs.size());
        
        // Audit log
        auditService.logDelete(ENTITY_NAME_PROGRAM, programId, oldValue, userId, username);
    }

    @Override
    @Transactional
    public RbsiInisiatifResponse createOrUpdateInisiatif(RbsiInisiatifRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        RbsiProgram program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));

        if (!program.getTahun().equals(request.getTahun())) {
            throw new BadRequestException("Tahun inisiatif harus sama dengan tahun program");
        }

        RbsiInisiatif existingInisiatif = inisiatifRepository
                .findByProgramIdAndTahunAndNomorInisiatifAndIsDeletedFalse(request.getProgramId(), request.getTahun(), request.getNomorInisiatif())
                .orElse(null);
        
        boolean isNew = (existingInisiatif == null);
        RbsiInisiatifResponse oldValue = isNew ? null : mapToInisiatifResponse(existingInisiatif);
        
        RbsiInisiatif inisiatif;
        if (existingInisiatif != null) {
            inisiatif = existingInisiatif;
        } else {
            // Create or use existing inisiatif group
            InisiatifGroup group;
            if (request.getGroupId() != null) {
                // Use existing group (same initiative, different year)
                group = inisiatifGroupRepository.findById(request.getGroupId())
                        .orElseThrow(() -> new ResourceNotFoundException("Inisiatif group tidak ditemukan"));
                
                // Validate group belongs to same RBSI
                if (!group.getRbsi().getId().equals(program.getRbsi().getId())) {
                    throw new BadRequestException("Inisiatif group tidak sesuai dengan RBSI");
                }
            } else {
                // Create new group
                group = InisiatifGroup.builder()
                        .rbsi(program.getRbsi())
                        .namaInisiatif(request.getNamaInisiatif())
                        .build();
                group = inisiatifGroupRepository.save(group);
            }

            inisiatif = RbsiInisiatif.builder()
                    .program(program)
                    .group(group)
                    .tahun(request.getTahun())
                    .nomorInisiatif(request.getNomorInisiatif())
                    .build();
        }

        inisiatif.setNamaInisiatif(request.getNamaInisiatif());
        // Update group nama as well
        inisiatif.getGroup().setNamaInisiatif(request.getNamaInisiatif());

        RbsiInisiatif savedInisiatif = inisiatifRepository.save(inisiatif);
        log.info("Inisiatif saved: {} with group: {}", savedInisiatif.getId(), savedInisiatif.getGroup().getId());
        
        // Audit log
        RbsiInisiatifResponse response = mapToInisiatifResponse(savedInisiatif);
        if (isNew) {
            auditService.logCreate(ENTITY_NAME_INISIATIF, savedInisiatif.getId(), response, userId, username);
        } else {
            auditService.logUpdate(ENTITY_NAME_INISIATIF, savedInisiatif.getId(), oldValue, response, userId, username);
        }
        
        return response;
    }

    @Override
    @Transactional
    public RbsiInisiatifResponse updateInisiatif(UUID inisiatifId, RbsiInisiatifRequest request) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        RbsiInisiatif inisiatif = inisiatifRepository.findById(inisiatifId)
                .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));

        // Capture old value untuk audit
        RbsiInisiatifResponse oldValue = mapToInisiatifResponse(inisiatif);

        if (!inisiatif.getProgram().getId().equals(request.getProgramId())) {
            throw new BadRequestException("Program id tidak sesuai dengan inisiatif");
        }

        if (!inisiatif.getNomorInisiatif().equals(request.getNomorInisiatif())
                || !inisiatif.getTahun().equals(request.getTahun())) {
            boolean exists = inisiatifRepository.existsByProgramIdAndTahunAndNomorInisiatifAndIsDeletedFalse(
                    request.getProgramId(), request.getTahun(), request.getNomorInisiatif()
            );
            if (exists) {
                throw new BadRequestException("Nomor inisiatif sudah digunakan di tahun tersebut");
            }
        }

        if (!inisiatif.getProgram().getTahun().equals(request.getTahun())) {
            throw new BadRequestException("Tahun inisiatif harus sama dengan tahun program");
        }

        inisiatif.setTahun(request.getTahun());
        inisiatif.setNomorInisiatif(request.getNomorInisiatif());
        inisiatif.setNamaInisiatif(request.getNamaInisiatif());

        // Handle group_id changes (frontend always sends group_id: null, existing UUID, or new UUID)
        UUID currentGroupId = inisiatif.getGroup() != null ? inisiatif.getGroup().getId() : null;
        UUID requestGroupId = request.getGroupId();
        
        // Only update if group changed
        if (!Objects.equals(requestGroupId, currentGroupId)) {
            if (requestGroupId == null) {
                // Frontend sent null -> create new group (separate from any existing group)
                RbsiProgram program = inisiatif.getProgram();
                InisiatifGroup newGroup = InisiatifGroup.builder()
                        .rbsi(program.getRbsi())
                        .namaInisiatif(request.getNamaInisiatif())
                        .build();
                newGroup = inisiatifGroupRepository.save(newGroup);
                inisiatif.setGroup(newGroup);
                log.info("Created new initiative group: {} for inisiatif: {}", newGroup.getId(), inisiatif.getId());
            } else {
                // Change to existing group (different UUID)
                InisiatifGroup group = inisiatifGroupRepository.findById(requestGroupId)
                        .orElseThrow(() -> new ResourceNotFoundException("Inisiatif group tidak ditemukan"));
                inisiatif.setGroup(group);
                log.info("Updated initiative group to: {} for inisiatif: {}", group.getId(), inisiatif.getId());
            }
        }
        // If requestGroupId equals currentGroupId, no change needed

        RbsiInisiatif savedInisiatif = inisiatifRepository.save(inisiatif);
        log.info("Inisiatif updated: {}", savedInisiatif.getId());
        
        // Audit log
        RbsiInisiatifResponse newValue = mapToInisiatifResponse(savedInisiatif);
        auditService.logUpdate(ENTITY_NAME_INISIATIF, inisiatifId, oldValue, newValue, userId, username);
        
        return newValue;
    }

    @Override
    @Transactional
    public void deleteInisiatif(UUID inisiatifId) {
        // Get user info di main thread sebelum async audit
        UUID userId = userContext.getCurrentUserId();
        String username = userContext.getCurrentUsername();
        
        RbsiInisiatif inisiatif = inisiatifRepository.findById(inisiatifId)
                .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));
        
        // Capture old value untuk audit sebelum delete
        RbsiInisiatifResponse oldValue = mapToInisiatifResponse(inisiatif);
        
        // Hard delete the inisiatif
        inisiatifRepository.delete(inisiatif);
        log.info("Inisiatif hard deleted: {}", inisiatifId);
        
        // Audit log
        auditService.logDelete(ENTITY_NAME_INISIATIF, inisiatifId, oldValue, userId, username);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InisiatifGroupResponse> getInisiatifGroups(UUID rbsiId) {
        // Validate RBSI exists
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<InisiatifGroup> groups = inisiatifGroupRepository.findByRbsiIdOrderByCreatedAtAsc(rbsiId);
        
        return groups.stream()
                .map(group -> {
                    // Get all initiatives in this group (sorted by year)
                    List<RbsiInisiatif> inisiatifs = group.getInisiatifs().stream()
                            .filter(ini -> !Boolean.TRUE.equals(ini.getIsDeleted()))
                            .sorted((a, b) -> a.getTahun().compareTo(b.getTahun()))
                            .toList();
                    
                    // Extract year list and nomor per year
                    List<Integer> tahunList = inisiatifs.stream()
                            .map(RbsiInisiatif::getTahun)
                            .distinct()
                            .sorted()
                            .toList();
                    
                    List<InisiatifGroupResponse.YearNomor> nomorByYear = inisiatifs.stream()
                            .map(ini -> InisiatifGroupResponse.YearNomor.builder()
                                    .tahun(ini.getTahun())
                                    .nomorInisiatif(ini.getNomorInisiatif())
                                    .programNomor(ini.getProgram() != null ? ini.getProgram().getNomorProgram() : null)
                                    .build())
                            .toList();
                    
                    return InisiatifGroupResponse.builder()
                            .id(group.getId())
                            .rbsiId(group.getRbsi().getId())
                            .namaInisiatif(group.getNamaInisiatif())
                            .keterangan(group.getKeterangan())
                            .tahunList(tahunList)
                            .nomorInisiatifByYear(nomorByYear)
                            .build();
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RbsiProgramResponse> getProgramsByRbsiAndTahun(UUID rbsiId, Integer tahun) {
        List<RbsiProgram> programs = programRepository.findByRbsiIdAndTahunWithInisiatifs(rbsiId, tahun);
        return programs.stream()
                .map(program -> mapToProgramResponse(program, true))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RbsiHistoryResponse> getHistory(UUID rbsiId) {
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<Integer> years = programRepository.findDistinctTahunByRbsiId(rbsiId);
        List<RbsiHistoryResponse> history = new ArrayList<>();

        for (Integer year : years) {
            List<RbsiProgramResponse> programs = getProgramsByRbsiAndTahun(rbsiId, year);
            history.add(RbsiHistoryResponse.builder()
                    .tahun(year)
                    .programs(programs)
                    .build());
        }

        return history;
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiHistoryResponse getHistoryByTahun(UUID rbsiId, Integer tahun) {
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<RbsiProgramResponse> programs = getProgramsByRbsiAndTahun(rbsiId, tahun);
        return RbsiHistoryResponse.builder()
                .tahun(tahun)
                .programs(programs)
                .build();
    }

    @Override
    @Transactional
    public List<RbsiProgramResponse> copyProgramsFromYear(UUID rbsiId, Integer fromTahun, Integer toTahun) {
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        // Check if target year already has programs
        List<RbsiProgram> existingPrograms = programRepository.findByRbsiIdAndTahunWithInisiatifs(rbsiId, toTahun);
        if (!existingPrograms.isEmpty()) {
            throw new BadRequestException("Tahun " + toTahun + " sudah memiliki program. Hapus terlebih dahulu jika ingin menyalin.");
        }

        // Get source programs
        List<RbsiProgram> sourcePrograms = programRepository.findByRbsiIdAndTahunWithInisiatifs(rbsiId, fromTahun);
        if (sourcePrograms.isEmpty()) {
            throw new BadRequestException("Tidak ada program di tahun " + fromTahun + " untuk disalin.");
        }

        List<RbsiProgramResponse> copiedPrograms = new ArrayList<>();

        for (RbsiProgram sourceProgram : sourcePrograms) {
            // Create new program
            RbsiProgram newProgram = RbsiProgram.builder()
                    .rbsi(rbsi)
                    .tahun(toTahun)
                    .nomorProgram(sourceProgram.getNomorProgram())
                    .namaProgram(sourceProgram.getNamaProgram())
                    .build();
            RbsiProgram savedProgram = programRepository.save(newProgram);

            // Copy inisiatifs
            List<RbsiInisiatif> sourceInisiatifs = inisiatifRepository
                    .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(sourceProgram.getId(), fromTahun);

            for (RbsiInisiatif sourceInisiatif : sourceInisiatifs) {
                RbsiInisiatif newInisiatif = RbsiInisiatif.builder()
                        .program(savedProgram)
                        .group(sourceInisiatif.getGroup())  // Use same group!
                        .tahun(toTahun)
                        .nomorInisiatif(sourceInisiatif.getNomorInisiatif())
                        .namaInisiatif(sourceInisiatif.getNamaInisiatif())
                        .build();
                inisiatifRepository.save(newInisiatif);
            }

            copiedPrograms.add(mapToProgramResponse(savedProgram, true));
        }

        log.info("Copied {} programs from year {} to year {} for RBSI {}", copiedPrograms.size(), fromTahun, toTahun, rbsiId);
        return copiedPrograms;
    }

    @Override
    @Transactional
    public RbsiProgramResponse copyProgram(UUID programId, Integer toTahun, String newNomorProgram) {
        RbsiProgram sourceProgram = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));

        Rbsi rbsi = sourceProgram.getRbsi();
        
        // Use new number if provided, otherwise use source number
        String targetNomorProgram = newNomorProgram != null ? newNomorProgram : sourceProgram.getNomorProgram();

        // Check if same nomor_program already exists in target year
        boolean exists = programRepository.existsByRbsiIdAndTahunAndNomorProgramAndIsDeletedFalse(
                rbsi.getId(), toTahun, targetNomorProgram);
        if (exists) {
            throw new BadRequestException("Program dengan nomor " + targetNomorProgram + 
                    " sudah ada di tahun " + toTahun);
        }

        // Create new program
        RbsiProgram newProgram = RbsiProgram.builder()
                .rbsi(rbsi)
                .tahun(toTahun)
                .nomorProgram(targetNomorProgram)
                .namaProgram(sourceProgram.getNamaProgram())
                .build();
        RbsiProgram savedProgram = programRepository.save(newProgram);

        // Copy inisiatifs - preserve original number suffix if program number changes
        List<RbsiInisiatif> sourceInisiatifs = inisiatifRepository
                .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(sourceProgram.getId(), sourceProgram.getTahun());

        String sourceNomorProgram = sourceProgram.getNomorProgram();
        for (RbsiInisiatif sourceInisiatif : sourceInisiatifs) {
            String newInisiatifNumber;
            String sourceNomorInisiatif = sourceInisiatif.getNomorInisiatif();
            
            if (targetNomorProgram.equals(sourceNomorProgram)) {
                // Same program number, keep original inisiatif number
                newInisiatifNumber = sourceNomorInisiatif;
            } else {
                // Different program number, replace prefix with new program number
                // e.g., "1.2a" with target "3" becomes "3.2a"
                if (sourceNomorInisiatif.startsWith(sourceNomorProgram + ".")) {
                    String suffix = sourceNomorInisiatif.substring(sourceNomorProgram.length() + 1);
                    newInisiatifNumber = targetNomorProgram + "." + suffix;
                } else {
                    // Fallback: keep original number if format doesn't match
                    newInisiatifNumber = sourceNomorInisiatif;
                }
            }

            RbsiInisiatif newInisiatif = RbsiInisiatif.builder()
                    .program(savedProgram)
                    .group(sourceInisiatif.getGroup())  // Use same group!
                    .tahun(toTahun)
                    .nomorInisiatif(newInisiatifNumber)
                    .namaInisiatif(sourceInisiatif.getNamaInisiatif())
                    .build();
            inisiatifRepository.save(newInisiatif);
        }

        log.info("Copied program {} to year {} as {} with {} inisiatifs", programId, toTahun, targetNomorProgram, sourceInisiatifs.size());
        return mapToProgramResponse(savedProgram, true);
    }

    @Override
    @Transactional
    public RbsiInisiatifResponse copyInisiatif(UUID inisiatifId, UUID toProgramId, String newNomorInisiatif) {
        RbsiInisiatif sourceInisiatif = inisiatifRepository.findById(inisiatifId)
                .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));

        RbsiProgram targetProgram = programRepository.findById(toProgramId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tujuan tidak ditemukan"));

        // Use new number if provided, otherwise use source number
        String targetNomorInisiatif = newNomorInisiatif != null ? newNomorInisiatif : sourceInisiatif.getNomorInisiatif();

        // Check if same nomor_inisiatif already exists in target program
        boolean exists = inisiatifRepository.existsByProgramIdAndTahunAndNomorInisiatifAndIsDeletedFalse(
                toProgramId, targetProgram.getTahun(), targetNomorInisiatif);
        if (exists) {
            throw new BadRequestException("Inisiatif dengan nomor " + targetNomorInisiatif +
                    " sudah ada di program tujuan");
        }

        // Create new inisiatif with SAME GROUP as source (this is the key!)
        RbsiInisiatif newInisiatif = RbsiInisiatif.builder()
                .program(targetProgram)
                .group(sourceInisiatif.getGroup())  // Use same group!
                .tahun(targetProgram.getTahun())
                .nomorInisiatif(targetNomorInisiatif)
                .namaInisiatif(sourceInisiatif.getNamaInisiatif())
                .build();
        RbsiInisiatif savedInisiatif = inisiatifRepository.save(newInisiatif);
        return mapToInisiatifResponse(savedInisiatif);
    }

    // ==================== KEP Methods ====================

    @Override
    @Transactional(readOnly = true)
    public List<RbsiKepResponse> getKepList(UUID rbsiId) {
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        return kepRepository.findByRbsiIdOrderByTahunPelaporanAsc(rbsiId).stream()
                .map(this::mapToKepResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RbsiKepResponse createKep(UUID rbsiId, RbsiKepRequest request) {
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        // Validate uniqueness
        if (kepRepository.existsByRbsiIdAndNomorKep(rbsiId, request.getNomorKep())) {
            throw new BadRequestException("Nomor KEP sudah digunakan");
        }
        if (kepRepository.existsByRbsiIdAndTahunPelaporan(rbsiId, request.getTahunPelaporan())) {
            throw new BadRequestException("Tahun pelaporan sudah digunakan");
        }

        // Create new KEP
        RbsiKep kep = RbsiKep.builder()
                .rbsi(rbsi)
                .nomorKep(request.getNomorKep())
                .tahunPelaporan(request.getTahunPelaporan())
                .build();
        RbsiKep savedKep = kepRepository.save(kep);

        // If copy_from_latest is true, copy progress from the latest KEP
        if (Boolean.TRUE.equals(request.getCopyFromLatest())) {
            copyProgressFromLatestKep(rbsiId, savedKep);
        }

        log.info("KEP created: {} for RBSI {}", savedKep.getId(), rbsiId);
        return mapToKepResponse(savedKep);
    }

    @Override
    @Transactional(readOnly = true)
    public KepProgressFullResponse getKepProgress(UUID rbsiId, Integer tahun) {
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<RbsiKep> kepList = kepRepository.findByRbsiIdOrderByTahunPelaporanAsc(rbsiId);
        List<RbsiKepResponse> kepResponses = kepList.stream()
                .map(this::mapToKepResponse)
                .collect(Collectors.toList());

        // Get inisiatifs for the specified year, or latest year if not specified
        Integer targetTahun = tahun != null ? tahun : programRepository.findMaxTahunByRbsiId(rbsiId);
        List<KepProgressFullResponse.InisiatifKepProgress> progressList = new ArrayList<>();

        if (targetTahun != null) {
            List<RbsiProgram> programs = programRepository.findByRbsiIdAndTahunWithInisiatifs(rbsiId, targetTahun);

            for (RbsiProgram program : programs) {
                List<RbsiInisiatif> inisiatifs = inisiatifRepository
                        .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(program.getId(), targetTahun);

                for (RbsiInisiatif inisiatif : inisiatifs) {
                    List<KepProgressFullResponse.KepProgressItem> kepProgressItems = new ArrayList<>();

                    for (RbsiKep kep : kepList) {
                        List<KepProgress> progressEntries = kepProgressRepository
                                .findByKepIdAndInisiatifGroupIdOrderByTahunAsc(kep.getId(), inisiatif.getGroup().getId());

                        List<KepProgressResponse.YearlyProgressResponse> yearlyProgress = progressEntries.stream()
                                .map(p -> KepProgressResponse.YearlyProgressResponse.builder()
                                        .tahun(p.getTahun())
                                        .status(p.getStatus().name())
                                        .build())
                                .collect(Collectors.toList());

                        kepProgressItems.add(KepProgressFullResponse.KepProgressItem.builder()
                                .kepId(kep.getId())
                                .nomorKep(kep.getNomorKep())
                                .tahunPelaporan(kep.getTahunPelaporan())
                                .yearlyProgress(yearlyProgress)
                                .build());
                    }

                    progressList.add(KepProgressFullResponse.InisiatifKepProgress.builder()
                            .inisiatifId(inisiatif.getGroup().getId())  // Use group ID for consistent tracking across years
                            .kepProgress(kepProgressItems)
                            .build());
                }
            }
        }

        return KepProgressFullResponse.builder()
                .rbsiId(rbsiId)
                .periode(rbsi.getPeriode())
                .kepList(kepResponses)
                .progress(progressList)
                .build();
    }

    @Override
    @Transactional
    public KepProgressResponse updateKepProgress(UUID rbsiId, UUID kepId, KepProgressRequest request) {
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        RbsiKep kep = kepRepository.findById(kepId)
                .orElseThrow(() -> new ResourceNotFoundException("KEP tidak ditemukan"));

        if (!kep.getRbsi().getId().equals(rbsiId)) {
            throw new BadRequestException("KEP tidak termasuk dalam RBSI ini");
        }

        RbsiInisiatif inisiatif = inisiatifRepository.findById(request.getInisiatifId())
                .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));

        // Get the group from the inisiatif
        InisiatifGroup group = inisiatif.getGroup();

        List<KepProgressResponse.YearlyProgressResponse> savedProgress = new ArrayList<>();

        for (KepProgressRequest.YearlyProgressItem item : request.getYearlyProgress()) {
            KepProgress progress = kepProgressRepository
                    .findByKepIdAndInisiatifGroupIdAndTahun(kepId, group.getId(), item.getTahun())
                    .orElseGet(() -> KepProgress.builder()
                            .kep(kep)
                            .inisiatifGroup(group)
                            .tahun(item.getTahun())
                            .build());

            progress.setStatus(KepProgress.ProgressStatus.valueOf(item.getStatus()));
            KepProgress saved = kepProgressRepository.save(progress);

            savedProgress.add(KepProgressResponse.YearlyProgressResponse.builder()
                    .tahun(saved.getTahun())
                    .status(saved.getStatus().name())
                    .build());
        }

        return KepProgressResponse.builder()
                .kepId(kepId)
                .nomorKep(kep.getNomorKep())
                .groupId(group.getId())
                .yearlyProgress(savedProgress)
                .updatedAt(kep.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional
    public BatchKepProgressResponse batchUpdateKepProgress(UUID rbsiId, BatchKepProgressRequest request) {
        // Validate RBSI exists
        rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        List<KepProgressResponse> updatedProgressList = new ArrayList<>();
        int successCount = 0;

        for (BatchKepProgressRequest.KepProgressUpdate update : request.getUpdates()) {
            try {
                log.info("Processing update Kep Progress");
                // Validate KEP exists and belongs to this RBSI
                RbsiKep kep = kepRepository.findById(update.getKepId())
                        .orElseThrow(() -> new ResourceNotFoundException("KEP " + update.getKepId() + " tidak ditemukan"));

                if (!kep.getRbsi().getId().equals(rbsiId)) {
                    log.warn("KEP {} tidak termasuk dalam RBSI {}", update.getKepId(), rbsiId);
                    continue;
                }

                // Get the group directly
                log.info("Looking for InisiatifGroup");
                InisiatifGroup group = inisiatifGroupRepository.findById(update.getGroupId())
                        .orElseThrow(() -> new ResourceNotFoundException("Group " + update.getGroupId() + " tidak ditemukan"));
                
                log.info("Group found: {}", group.getId());

                List<KepProgressResponse.YearlyProgressResponse> savedProgress = new ArrayList<>();

                // Update or create progress for each year
                for (BatchKepProgressRequest.KepProgressUpdate.YearlyProgressItem item : update.getYearlyProgress()) {
                    KepProgress progress = kepProgressRepository
                            .findByKepIdAndInisiatifGroupIdAndTahun(update.getKepId(), group.getId(), item.getTahun())
                            .orElseGet(() -> KepProgress.builder()
                                    .kep(kep)
                                    .inisiatifGroup(group)
                                    .tahun(item.getTahun())
                                    .build());

                    progress.setStatus(KepProgress.ProgressStatus.valueOf(item.getStatus().toLowerCase()));
                    KepProgress saved = kepProgressRepository.save(progress);

                    savedProgress.add(KepProgressResponse.YearlyProgressResponse.builder()
                            .tahun(saved.getTahun())
                            .status(saved.getStatus().name().toLowerCase())
                            .build());
                }

                updatedProgressList.add(KepProgressResponse.builder()
                        .kepId(update.getKepId())
                        .nomorKep(kep.getNomorKep())
                        .groupId(update.getGroupId())
                        .yearlyProgress(savedProgress)
                        .updatedAt(kep.getUpdatedAt())
                        .build());

                successCount++;

            } catch (Exception e) {
                log.error("❌ Error updating progress for KEP {} with Group ID {}: {}", 
                    update.getKepId(), update.getGroupId(), e.getMessage(), e);
                // Continue with next update instead of failing entire batch
            }
        }

        log.info("Batch update completed: {} out of {} updates successful", successCount, request.getUpdates().size());

        return BatchKepProgressResponse.builder()
                .totalUpdated(successCount)
                .updatedProgress(updatedProgressList)
                .message(String.format("Successfully updated %d out of %d KEP progress records", successCount, request.getUpdates().size()))
                .build();
    }

    private void copyProgressFromLatestKep(UUID rbsiId, RbsiKep newKep) {
        // Find the previous latest KEP
        List<RbsiKep> existingKeps = kepRepository.findByRbsiIdOrderByTahunPelaporanAsc(rbsiId);
        if (existingKeps.size() <= 1) {
            return; // No previous KEP to copy from
        }

        // Get the second-to-last KEP (the one before the newly created one)
        RbsiKep previousKep = existingKeps.get(existingKeps.size() - 2);

        // CRITICAL: Get initiative groups that have active initiatives in the TARGET YEAR (newKep's year)
        // We must check if the group has initiatives in the specific year, not just "any year"
        Integer targetYear = newKep.getTahunPelaporan();
        
        // Get all programs in the target year
        List<RbsiProgram> programsInTargetYear = programRepository.findByRbsiIdAndTahunWithInisiatifs(rbsiId, targetYear);
        
        // Collect group IDs that have at least one active initiative in the target year
        Set<UUID> activeGroupIdsInTargetYear = new HashSet<>();
        for (RbsiProgram program : programsInTargetYear) {
            List<RbsiInisiatif> activeInisiatifs = inisiatifRepository
                    .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(program.getId(), targetYear);
            
            for (RbsiInisiatif inisiatif : activeInisiatifs) {
                if (inisiatif.getGroup() != null) {
                    activeGroupIdsInTargetYear.add(inisiatif.getGroup().getId());
                }
            }
        }

        // Copy progress entries only for initiative groups that exist in the target year
        List<KepProgress> previousProgress = kepProgressRepository.findByKepIdOrderByInisiatifGroupIdAscTahunAsc(previousKep.getId());
        int copiedCount = 0;
        int skippedCount = 0;
        
        for (KepProgress p : previousProgress) {
            UUID groupId = p.getInisiatifGroup().getId();
            
            // Only copy if the initiative group has active initiatives in the TARGET YEAR
            if (activeGroupIdsInTargetYear.contains(groupId)) {
                KepProgress newProgress = KepProgress.builder()
                        .kep(newKep)
                        .inisiatifGroup(p.getInisiatifGroup())
                        .tahun(p.getTahun())
                        .status(p.getStatus())
                        .build();
                kepProgressRepository.save(newProgress);
                copiedCount++;
            } else {
                skippedCount++;
                log.debug("Skipped progress for group {} (no active initiatives in year {})", groupId, targetYear);
            }
        }

        log.info("Copied {} progress entries from KEP {} to KEP {} for year {} (skipped {} groups without initiatives in target year)", 
                 copiedCount, previousKep.getId(), newKep.getId(), targetYear, skippedCount);
    }

    private RbsiKepResponse mapToKepResponse(RbsiKep kep) {
        return RbsiKepResponse.builder()
                .id(kep.getId())
                .rbsiId(kep.getRbsi().getId())
                .nomorKep(kep.getNomorKep())
                .tahunPelaporan(kep.getTahunPelaporan())
                .createdAt(kep.getCreatedAt())
                .updatedAt(kep.getUpdatedAt())
                .build();
    }

    private void upsertInisiatifs(RbsiProgram program, List<RbsiInisiatifItemRequest> inisiatifs) {
        Map<String, RbsiInisiatif> existing = inisiatifRepository
            .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(program.getId(), program.getTahun()).stream()
            .collect(Collectors.toMap(RbsiInisiatif::getNomorInisiatif, item -> item, (a, b) -> a));

        for (RbsiInisiatifItemRequest item : inisiatifs) {
            RbsiInisiatif inisiatif = existing.getOrDefault(item.getNomorInisiatif(), RbsiInisiatif.builder()
                    .program(program)
                    .tahun(program.getTahun())
                    .nomorInisiatif(item.getNomorInisiatif())
                    .build());

            inisiatif.setNamaInisiatif(item.getNamaInisiatif());
            inisiatifRepository.save(inisiatif);
        }
    }

    private RbsiResponse mapToRbsiResponse(Rbsi rbsi, List<RbsiProgramResponse> programs) {
        return RbsiResponse.builder()
                .id(rbsi.getId())
                .periode(rbsi.getPeriode())
                .createdAt(rbsi.getCreatedAt())
                .updatedAt(rbsi.getUpdatedAt())
                .programs(programs)
                .build();
    }

    private RbsiProgramResponse mapToProgramResponse(RbsiProgram program, boolean includeInisiatif) {
        List<RbsiInisiatifResponse> inisiatifs = null;
        if (includeInisiatif) {
            inisiatifs = inisiatifRepository
                    .findByProgramIdAndTahunAndIsDeletedFalseOrderByNomorInisiatifAsc(program.getId(), program.getTahun())
                    .stream()
                    .map(this::mapToInisiatifResponse)
                    .collect(Collectors.toList());
        }

        return RbsiProgramResponse.builder()
                .id(program.getId())
                .rbsiId(program.getRbsi().getId())
                .tahun(program.getTahun())
                .nomorProgram(program.getNomorProgram())
                .namaProgram(program.getNamaProgram())
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .inisiatifs(inisiatifs)
                .build();
    }

    private RbsiInisiatifResponse mapToInisiatifResponse(RbsiInisiatif inisiatif) {
        return RbsiInisiatifResponse.builder()
                .id(inisiatif.getId())
                .groupId(inisiatif.getGroup() != null ? inisiatif.getGroup().getId() : null)
                .programId(inisiatif.getProgram().getId())
                .tahun(inisiatif.getTahun())
                .nomorInisiatif(inisiatif.getNomorInisiatif())
                .namaInisiatif(inisiatif.getNamaInisiatif())
                .createdAt(inisiatif.getCreatedAt())
                .updatedAt(inisiatif.getUpdatedAt())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiMonitoringResponse getMonitoringData(UUID rbsiId) {
        // Validate RBSI exists
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        // Get KEP list
        List<RbsiKep> kepList = kepRepository.findByRbsiIdOrderByTahunPelaporanAsc(rbsiId);
        List<RbsiMonitoringResponse.KepInfo> kepInfoList = kepList.stream()
                .map(kep -> RbsiMonitoringResponse.KepInfo.builder()
                        .id(kep.getId())
                        .nomorKep(kep.getNomorKep())
                        .tahunPelaporan(kep.getTahunPelaporan())
                        .build())
                .collect(Collectors.toList());

        // Extract available KEP years for deleted badge detection
        Set<Integer> availableKepYears = kepList.stream()
                .map(RbsiKep::getTahunPelaporan)
                .collect(Collectors.toSet());

        // Get all programs across all years for this RBSI
        List<RbsiProgram> allPrograms = programRepository.findByRbsiIdAndIsDeletedFalseOrderByNomorProgramAsc(rbsiId);
        
        // Group programs by nomor_program
        Map<String, List<RbsiProgram>> programsByNomor = allPrograms.stream()
                .collect(Collectors.groupingBy(RbsiProgram::getNomorProgram));

        List<RbsiMonitoringResponse.ProgramMonitoring> programMonitoringList = new ArrayList<>();

        for (Map.Entry<String, List<RbsiProgram>> entry : programsByNomor.entrySet()) {
            String nomorProgram = entry.getKey();
            List<RbsiProgram> programVersions = entry.getValue();

            // Build versions by year map
            Map<Integer, RbsiMonitoringResponse.ProgramVersion> versionsByYear = programVersions.stream()
                    .collect(Collectors.toMap(
                            RbsiProgram::getTahun,
                            p -> RbsiMonitoringResponse.ProgramVersion.builder()
                                    .id(p.getId())
                                    .namaProgram(p.getNamaProgram())
                                    .tahun(p.getTahun())
                                    .build()
                    ));

            // Get all initiatives for all years of this program
            List<UUID> programIds = programVersions.stream()
                    .map(RbsiProgram::getId)
                    .collect(Collectors.toList());

            List<RbsiInisiatif> allInisiatifs = inisiatifRepository.findByProgramIdInAndIsDeletedFalse(programIds);

            // Group initiatives by group_id
            Map<UUID, List<RbsiInisiatif>> inisiatifsByGroup = allInisiatifs.stream()
                    .filter(i -> i.getGroup() != null)
                    .collect(Collectors.groupingBy(i -> i.getGroup().getId()));

            List<RbsiMonitoringResponse.InitiativeMonitoring> initiativeMonitoringList = new ArrayList<>();

            for (Map.Entry<UUID, List<RbsiInisiatif>> iniEntry : inisiatifsByGroup.entrySet()) {
                UUID groupId = iniEntry.getKey();
                List<RbsiInisiatif> initiativeVersions = iniEntry.getValue();

                // Sort by year for comparison
                List<RbsiInisiatif> sortedVersions = initiativeVersions.stream()
                        .sorted(Comparator.comparing(RbsiInisiatif::getTahun))
                        .collect(Collectors.toList());

                // Build versions by year map with status badges
                Map<Integer, RbsiMonitoringResponse.InitiativeVersion> iniVersionsByYear = new java.util.HashMap<>();
                
                for (int idx = 0; idx < sortedVersions.size(); idx++) {
                    RbsiInisiatif current = sortedVersions.get(idx);
                    String statusBadge = null;
                    
                    // Check if this is last year for this initiative (priority 1: deleted)
                    // Only mark as deleted if next year has KEP but initiative doesn't exist
                    boolean isLastVersion = (idx == sortedVersions.size() - 1);
                    int nextYear = current.getTahun() + 1;
                    if (isLastVersion && availableKepYears.contains(nextYear)) {
                        statusBadge = "deleted";
                    } else if (idx == 0) {
                        // First year for this group = new
                        statusBadge = "new";
                    } else {
                        // Compare with previous year - only check nama changes
                        RbsiInisiatif previous = sortedVersions.get(idx - 1);
                        boolean namaChanged = !current.getNamaInisiatif().equals(previous.getNamaInisiatif());
                        
                        if (namaChanged) {
                            statusBadge = "modified";
                        }
                    }
                    
                    iniVersionsByYear.put(current.getTahun(),
                            RbsiMonitoringResponse.InitiativeVersion.builder()
                                    .id(current.getId())
                                    .nomorInisiatif(current.getNomorInisiatif())
                                    .namaInisiatif(current.getNamaInisiatif())
                                    .tahun(current.getTahun())
                                    .statusBadge(statusBadge)
                                    .build()
                    );
                }

                // Get progress data for this initiative group across all KEPs
                Map<UUID, RbsiMonitoringResponse.KepProgress> progressByKep = new java.util.HashMap<>();

                for (RbsiKep kep : kepList) {
                    List<KepProgress> progressList = kepProgressRepository
                            .findByKepIdAndInisiatifGroupIdOrderByTahunAsc(kep.getId(), groupId);

                    List<RbsiMonitoringResponse.YearlyProgress> yearlyProgressList = progressList.stream()
                            .map(p -> RbsiMonitoringResponse.YearlyProgress.builder()
                                    .tahun(p.getTahun())
                                    .status(p.getStatus().name().toLowerCase())
                                    .build())
                            .collect(Collectors.toList());

                    if (!yearlyProgressList.isEmpty()) {
                        progressByKep.put(kep.getId(), RbsiMonitoringResponse.KepProgress.builder()
                                .yearlyProgress(yearlyProgressList)
                                .build());
                    }
                }

                initiativeMonitoringList.add(RbsiMonitoringResponse.InitiativeMonitoring.builder()
                        .groupId(groupId)
                        .versionsByYear(iniVersionsByYear)
                        .progressByKep(progressByKep)
                        .build());
            }

            // Sort initiatives using natural sort based on nomor_inisiatif from any version
            initiativeMonitoringList.sort(Comparator.comparing(
                    ini -> {
                        // Get nomor_inisiatif from the first available version
                        if (ini.getVersionsByYear() != null && !ini.getVersionsByYear().isEmpty()) {
                            return ini.getVersionsByYear().values().iterator().next().getNomorInisiatif();
                        }
                        return "";
                    },
                    NomorComparator::compare
            ));

            programMonitoringList.add(RbsiMonitoringResponse.ProgramMonitoring.builder()
                    .nomorProgram(nomorProgram)
                    .versionsByYear(versionsByYear)
                    .initiatives(initiativeMonitoringList)
                    .build());
        }

        // Sort programs using natural sort for version numbers (3.1, 3.2, 3.11 instead of 3.1, 3.11, 3.2)
        programMonitoringList.sort(Comparator.comparing(
                RbsiMonitoringResponse.ProgramMonitoring::getNomorProgram,
                NomorComparator::compare
        ));

        return RbsiMonitoringResponse.builder()
                .rbsiId(rbsiId)
                .periode(rbsi.getPeriode())
                .kepList(kepInfoList)
                .programs(programMonitoringList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiAnalyticsResponse getAnalytics(UUID rbsiId, RbsiAnalyticsRequest request) {
        Rbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException(ENTITY_NAME_RBSI + " not found with id: " + rbsiId));

        Integer inputTahun1 = request.getTahun1();
        Integer inputTahun2 = request.getTahun2();

        // Ensure tahun1 < tahun2
        final Integer tahun1 = inputTahun1 <= inputTahun2 ? inputTahun1 : inputTahun2;
        final Integer tahun2 = inputTahun1 <= inputTahun2 ? inputTahun2 : inputTahun1;

        // Get KEPs for both years
        RbsiKep kep1 = kepRepository.findByRbsiIdAndTahunPelaporan(rbsiId, tahun1)
                .orElseThrow(() -> new BadRequestException("KEP untuk tahun " + tahun1 + " tidak ditemukan"));
        RbsiKep kep2 = kepRepository.findByRbsiIdAndTahunPelaporan(rbsiId, tahun2)
                .orElseThrow(() -> new BadRequestException("KEP untuk tahun " + tahun2 + " tidak ditemukan"));

        // Get all progress data for both KEPs in one query
        List<UUID> kepIds = List.of(kep1.getId(), kep2.getId());
        List<KepProgress> allProgress = kepProgressRepository.findByKepIdIn(kepIds);

        // Group progress by KEP and year for counting realized status (status != none)
        Map<UUID, Map<Integer, List<KepProgress>>> progressByKepAndYear = allProgress.stream()
                .collect(Collectors.groupingBy(
                        kp -> kp.getKep().getId(),
                        Collectors.groupingBy(KepProgress::getTahun)
                ));

        // Calculate counts for each KEP by year
        RbsiAnalyticsResponse.KepEvaluation eval1 = buildKepEvaluation(kep1, progressByKepAndYear.getOrDefault(kep1.getId(), Map.of()), null);
        RbsiAnalyticsResponse.KepEvaluation eval2 = buildKepEvaluation(kep2, progressByKepAndYear.getOrDefault(kep2.getId(), Map.of()), 
                buildProgressChanges(allProgress, kep1.getId(), kep2.getId()));

        return RbsiAnalyticsResponse.builder()
                .rbsiId(rbsiId)
                .periode(rbsi.getPeriode())
                .evaluations(List.of(eval1, eval2))
                .build();
    }

    private RbsiAnalyticsResponse.KepEvaluation buildKepEvaluation(
            RbsiKep kep, 
            Map<Integer, List<KepProgress>> progressByYear,
            RbsiAnalyticsResponse.ProgressChanges changes) {
        
        Map<Integer, Integer> countByYear = new java.util.HashMap<>();
        int total = 0;

        for (Map.Entry<Integer, List<KepProgress>> entry : progressByYear.entrySet()) {
            int countRealized = (int) entry.getValue().stream()
                    .filter(kp -> !"none".equalsIgnoreCase(kp.getStatus().name()))
                    .count();
            countByYear.put(entry.getKey(), countRealized);
            total += countRealized;
        }

        return RbsiAnalyticsResponse.KepEvaluation.builder()
                .kepId(kep.getId())
                .nomorKep(kep.getNomorKep())
                .tahunPelaporan(kep.getTahunPelaporan())
                .total(total)
                .countByYear(countByYear)
                .changes(changes)
                .build();
    }

    private RbsiAnalyticsResponse.ProgressChanges buildProgressChanges(
            List<KepProgress> allProgress,
            UUID kep1Id,
            UUID kep2Id) {
        
        // Group by inisiatif_group_id and tahun for each KEP
        Map<UUID, Map<Integer, String>> statusByGroupAndYearKep1 = allProgress.stream()
                .filter(kp -> kp.getKep().getId().equals(kep1Id))
                .collect(Collectors.groupingBy(
                        kp -> kp.getInisiatifGroup().getId(),
                        Collectors.toMap(KepProgress::getTahun, kp -> kp.getStatus().name().toLowerCase())
                ));

        Map<UUID, Map<Integer, String>> statusByGroupAndYearKep2 = allProgress.stream()
                .filter(kp -> kp.getKep().getId().equals(kep2Id))
                .collect(Collectors.groupingBy(
                        kp -> kp.getInisiatifGroup().getId(),
                        Collectors.toMap(KepProgress::getTahun, kp -> kp.getStatus().name().toLowerCase())
                ));

        // Get all group IDs from both KEPs
        Set<UUID> allGroupIds = new java.util.HashSet<>();
        allGroupIds.addAll(statusByGroupAndYearKep1.keySet());
        allGroupIds.addAll(statusByGroupAndYearKep2.keySet());

        // Fetch inisiatif details by group IDs (CORRECT QUERY this time!)
        List<RbsiInisiatif> inisiatifs = inisiatifRepository.findByGroupIdIn(new ArrayList<>(allGroupIds));
        
        // Group by group_id, take latest version (highest tahun)
        Map<UUID, RbsiInisiatif> inisiatifByGroup = inisiatifs.stream()
                .collect(Collectors.toMap(
                        i -> i.getGroup().getId(),
                        i -> i,
                        (i1, i2) -> i1.getTahun() > i2.getTahun() ? i1 : i2
                ));

        // Get all years from KEP2
        Set<Integer> allYears = statusByGroupAndYearKep2.values().stream()
                .flatMap(m -> m.keySet().stream())
                .collect(Collectors.toSet());

        // Compare status per year - DON'T CHANGE THIS LOGIC, IT'S CORRECT!
        Map<Integer, RbsiAnalyticsResponse.YearChange> changesByYear = new java.util.HashMap<>();
        boolean hasChanges = false;

        for (Integer year : allYears) {
            int added = 0;
            int removed = 0;
            List<RbsiAnalyticsResponse.InitiativeDetail> addedInitiatives = new java.util.ArrayList<>();
            List<RbsiAnalyticsResponse.InitiativeDetail> removedInitiatives = new java.util.ArrayList<>();

            for (UUID groupId : statusByGroupAndYearKep2.keySet()) {
                String statusKep1 = statusByGroupAndYearKep1.getOrDefault(groupId, Map.of()).get(year);
                String statusKep2 = statusByGroupAndYearKep2.getOrDefault(groupId, Map.of()).get(year);

                if (statusKep1 == null) statusKep1 = "none";
                if (statusKep2 == null) statusKep2 = "none";

                boolean wasRealized = !statusKep1.equals("none");
                boolean isRealized = !statusKep2.equals("none");

                if (!wasRealized && isRealized) {
                    added++;
                    // Add initiative detail
                    RbsiInisiatif inisiatif = inisiatifByGroup.get(groupId);
                    if (inisiatif != null) {
                        addedInitiatives.add(RbsiAnalyticsResponse.InitiativeDetail.builder()
                                .groupId(groupId)
                                .nomorInisiatif(inisiatif.getNomorInisiatif())
                                .namaInisiatif(inisiatif.getNamaInisiatif())
                                .nomorProgram(inisiatif.getProgram().getNomorProgram())
                                .build());
                    }
                } else if (wasRealized && !isRealized) {
                    removed++;
                    // Add initiative detail
                    RbsiInisiatif inisiatif = inisiatifByGroup.get(groupId);
                    if (inisiatif != null) {
                        removedInitiatives.add(RbsiAnalyticsResponse.InitiativeDetail.builder()
                                .groupId(groupId)
                                .nomorInisiatif(inisiatif.getNomorInisiatif())
                                .namaInisiatif(inisiatif.getNamaInisiatif())
                                .nomorProgram(inisiatif.getProgram().getNomorProgram())
                                .build());
                    }
                }
            }

            // Also check for groups that existed in KEP1 but not in KEP2
            for (UUID groupId : statusByGroupAndYearKep1.keySet()) {
                if (!statusByGroupAndYearKep2.containsKey(groupId)) {
                    String statusKep1 = statusByGroupAndYearKep1.get(groupId).get(year);
                    if (statusKep1 != null && !statusKep1.equals("none")) {
                        removed++;
                        // Add initiative detail
                        RbsiInisiatif inisiatif = inisiatifByGroup.get(groupId);
                        if (inisiatif != null) {
                            removedInitiatives.add(RbsiAnalyticsResponse.InitiativeDetail.builder()
                                    .groupId(groupId)
                                    .nomorInisiatif(inisiatif.getNomorInisiatif())
                                    .namaInisiatif(inisiatif.getNamaInisiatif())
                                    .nomorProgram(inisiatif.getProgram().getNomorProgram())
                                    .build());
                        }
                    }
                }
            }

            if (added > 0 || removed > 0) {
                hasChanges = true;
                StringBuilder summary = new StringBuilder();
                if (added > 0) {
                    summary.append("+").append(added).append(" inisiatif");
                }
                if (removed > 0) {
                    if (summary.length() > 0) summary.append(", ");
                    summary.append("-").append(removed).append(" inisiatif");
                }

                // Sort initiatives by nomor
                addedInitiatives.sort(Comparator.comparing(RbsiAnalyticsResponse.InitiativeDetail::getNomorProgram)
                        .thenComparing(RbsiAnalyticsResponse.InitiativeDetail::getNomorInisiatif));
                removedInitiatives.sort(Comparator.comparing(RbsiAnalyticsResponse.InitiativeDetail::getNomorProgram)
                        .thenComparing(RbsiAnalyticsResponse.InitiativeDetail::getNomorInisiatif));

                changesByYear.put(year, RbsiAnalyticsResponse.YearChange.builder()
                        .added(added)
                        .removed(removed)
                        .summary(summary.toString())
                        .addedInitiatives(addedInitiatives)
                        .removedInitiatives(removedInitiatives)
                        .build());
            }
        }

        return RbsiAnalyticsResponse.ProgressChanges.builder()
                .hasChanges(hasChanges)
                .changesByYear(changesByYear)
                .build();
    }
}
