package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.rbsi.*;
import com.pcs8.orientasi.domain.entity.MstInitiative;
import com.pcs8.orientasi.domain.entity.MstProgram;
import com.pcs8.orientasi.domain.entity.MstRbsi;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.InitiativeRepository;
import com.pcs8.orientasi.repository.ProgramRepository;
import com.pcs8.orientasi.repository.RbsiRepository;
import com.pcs8.orientasi.service.RbsiService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RbsiServiceImpl implements RbsiService {

    private final RbsiRepository rbsiRepository;
    private final ProgramRepository programRepository;
    private final InitiativeRepository initiativeRepository;

    @Override
    @Transactional
    public RbsiResponse createRbsi(RbsiCreateRequest request) {
        log.info("Creating new RBSI with periode: {}", request.getPeriode());

        // Validasi periode unik
        if (rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new BadRequestException("RBSI dengan periode " + request.getPeriode() + " sudah ada");
        }

        MstRbsi rbsi = MstRbsi.builder()
                .periode(request.getPeriode())
                .isActive(true)
                .build();

        MstRbsi savedRbsi = rbsiRepository.save(rbsi);
        log.info("RBSI created successfully with ID: {}", savedRbsi.getId());

        return mapToRbsiResponse(savedRbsi);
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiListResponse getAllRbsi(int page, int size) {
        log.info("Fetching RBSI list - page: {}, size: {}", page, size);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<MstRbsi> rbsiPage = rbsiRepository.findAllActive(pageable);

        List<RbsiResponse> responseList = rbsiPage.getContent().stream()
                .map(this::mapToRbsiResponse)
                .collect(Collectors.toList());

        return RbsiListResponse.builder()
                .data(responseList)
                .totalItems(rbsiPage.getTotalElements())
                .totalPages(rbsiPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiResponse getRbsiById(UUID id) {
        log.info("Fetching RBSI by ID: {}", id);

        MstRbsi rbsi = rbsiRepository.findByIdAndActive(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan dengan ID: " + id));

        return mapToRbsiResponse(rbsi);
    }

    @Override
    @Transactional
    public ProgramResponse createProgram(ProgramCreateRequest request) {
        log.info("Creating new Program for RBSI ID: {}", request.getRbsiId());

        // Validasi RBSI exists
        MstRbsi rbsi = rbsiRepository.findByIdAndActive(request.getRbsiId())
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan dengan ID: " + request.getRbsiId()));

        // Tentukan sequence order
        Integer newSequence;
        if (request.getInsertAfterSequence() != null) {
            newSequence = request.getInsertAfterSequence() + 1;
            // Re-number programs yang ada setelah posisi insert
            reNumberProgramsAfterSequence(rbsi.getId(), request.getYearVersion(), newSequence);
        } else {
            newSequence = programRepository.findMaxSequenceOrder(rbsi.getId(), request.getYearVersion()) + 1;
        }

        // Generate program number (format: 3.X)
        String programNumber = "3." + newSequence;

        MstProgram program = MstProgram.builder()
                .rbsi(rbsi)
                .programNumber(programNumber)
                .sequenceOrder(newSequence)
                .name(request.getName())
                .description(request.getDescription())
                .yearVersion(request.getYearVersion())
                .startDate(request.getStartDate() != null ? request.getStartDate() : LocalDateTime.now())
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .isActive(true)
                .build();

        MstProgram savedProgram = programRepository.save(program);
        log.info("Program created with ID: {} and number: {}", savedProgram.getId(), programNumber);

        // Create initiatives jika ada
        List<MstInitiative> savedInitiatives = new ArrayList<>();
        if (request.getInitiatives() != null && !request.getInitiatives().isEmpty()) {
            savedInitiatives = createInitiativesForProgram(savedProgram, request.getInitiatives(), request.getYearVersion());
        }

        return mapToProgramResponse(savedProgram, savedInitiatives);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramListResponse getProgramsByYearAndRbsi(UUID rbsiId, Integer year, int page, int size) {
        log.info("Fetching Programs for RBSI ID: {}, Year: {}", rbsiId, year);

        // Validasi RBSI exists
        MstRbsi rbsi = rbsiRepository.findByIdAndActive(rbsiId)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan dengan ID: " + rbsiId));

        Pageable pageable = PageRequest.of(page, size);
        Page<MstProgram> programPage = programRepository.findByRbsiIdAndYearVersion(rbsiId, year, pageable);

        List<ProgramResponse> responseList = programPage.getContent().stream()
                .map(program -> {
                    List<MstInitiative> initiatives = initiativeRepository
                            .findByProgramIdAndYearVersion(program.getId(), year);
                    return mapToProgramResponse(program, initiatives);
                })
                .collect(Collectors.toList());

        return ProgramListResponse.builder()
                .data(responseList)
                .yearVersion(year)
                .rbsiPeriode(rbsi.getPeriode())
                .totalItems(programPage.getTotalElements())
                .totalPages(programPage.getTotalPages())
                .currentPage(page)
                .pageSize(size)
                .build();
    }

    @Override
    @Transactional
    public InitiativeResponse addInitiativeToProgram(UUID programId, InitiativeCreateRequest request, Integer yearVersion) {
        log.info("Adding initiative to Program ID: {}", programId);

        MstProgram program = programRepository.findByIdAndActive(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan dengan ID: " + programId));

        // Tentukan sequence order
        Integer newSequence;
        if (request.getInsertAfterSequence() != null) {
            newSequence = request.getInsertAfterSequence() + 1;
            // Re-number initiatives yang ada setelah posisi insert
            reNumberInitiativesAfterSequence(programId, yearVersion, newSequence);
        } else {
            newSequence = initiativeRepository.findMaxSequenceOrder(programId, yearVersion) + 1;
        }

        // Generate initiative number (format: 3.X.Y)
        String initiativeNumber = program.getProgramNumber() + "." + newSequence;

        MstInitiative initiative = MstInitiative.builder()
                .program(program)
                .initiativeNumber(initiativeNumber)
                .sequenceOrder(newSequence)
                .name(request.getName())
                .description(request.getDescription())
                .yearVersion(yearVersion)
                .submitDate(request.getSubmitDate())
                .documentLink(request.getDocumentLink())
                .status(request.getStatus() != null ? request.getStatus() : "pending")
                .pksiRelationId(request.getPksiRelationId())
                .isActive(true)
                .build();

        MstInitiative savedInitiative = initiativeRepository.save(initiative);
        log.info("Initiative created with ID: {} and number: {}", savedInitiative.getId(), initiativeNumber);

        return mapToInitiativeResponse(savedInitiative);
    }

    // === Private Helper Methods ===

    private void reNumberProgramsAfterSequence(UUID rbsiId, Integer yearVersion, Integer fromSequence) {
        log.info("Re-numbering programs from sequence {} for RBSI: {}", fromSequence, rbsiId);

        List<MstProgram> programsToUpdate = programRepository.findProgramsAfterSequence(rbsiId, yearVersion, fromSequence - 1);

        for (MstProgram program : programsToUpdate) {
            int newSequence = program.getSequenceOrder() + 1;
            program.setSequenceOrder(newSequence);
            program.setProgramNumber("3." + newSequence);

            // Update initiative numbers juga
            List<MstInitiative> initiatives = initiativeRepository
                    .findByProgramIdAndYearVersion(program.getId(), yearVersion);
            for (MstInitiative initiative : initiatives) {
                initiative.setInitiativeNumber(program.getProgramNumber() + "." + initiative.getSequenceOrder());
                initiativeRepository.save(initiative);
            }

            programRepository.save(program);
        }
    }

    private void reNumberInitiativesAfterSequence(UUID programId, Integer yearVersion, Integer fromSequence) {
        log.info("Re-numbering initiatives from sequence {} for Program: {}", fromSequence, programId);

        MstProgram program = programRepository.findByIdAndActive(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));

        List<MstInitiative> initiatives = initiativeRepository
                .findByProgramIdAndYearVersion(programId, yearVersion);

        for (MstInitiative initiative : initiatives) {
            if (initiative.getSequenceOrder() >= fromSequence) {
                int newSequence = initiative.getSequenceOrder() + 1;
                initiative.setSequenceOrder(newSequence);
                initiative.setInitiativeNumber(program.getProgramNumber() + "." + newSequence);
                initiativeRepository.save(initiative);
            }
        }
    }

    private List<MstInitiative> createInitiativesForProgram(MstProgram program, 
                                                             List<InitiativeCreateRequest> requests, 
                                                             Integer yearVersion) {
        List<MstInitiative> savedInitiatives = new ArrayList<>();
        int sequenceOrder = 1;

        for (InitiativeCreateRequest request : requests) {
            String initiativeNumber = program.getProgramNumber() + "." + sequenceOrder;

            MstInitiative initiative = MstInitiative.builder()
                    .program(program)
                    .initiativeNumber(initiativeNumber)
                    .sequenceOrder(sequenceOrder)
                    .name(request.getName())
                    .description(request.getDescription())
                    .yearVersion(yearVersion)
                    .submitDate(request.getSubmitDate())
                    .documentLink(request.getDocumentLink())
                    .status(request.getStatus() != null ? request.getStatus() : "pending")
                    .pksiRelationId(request.getPksiRelationId())
                    .isActive(true)
                    .build();

            savedInitiatives.add(initiativeRepository.save(initiative));
            sequenceOrder++;
        }

        return savedInitiatives;
    }

    private RbsiResponse mapToRbsiResponse(MstRbsi rbsi) {
        Long totalPrograms = programRepository.countByRbsiId(rbsi.getId());

        return RbsiResponse.builder()
                .id(rbsi.getId())
                .periode(rbsi.getPeriode())
                .isActive(rbsi.getIsActive())
                .totalPrograms(totalPrograms.intValue())
                .createdAt(rbsi.getCreatedAt())
                .updatedAt(rbsi.getUpdatedAt())
                .build();
    }

    private ProgramResponse mapToProgramResponse(MstProgram program, List<MstInitiative> initiatives) {
        List<InitiativeResponse> initiativeResponses = initiatives.stream()
                .map(this::mapToInitiativeResponse)
                .collect(Collectors.toList());

        return ProgramResponse.builder()
                .id(program.getId())
                .rbsiId(program.getRbsi().getId())
                .programNumber(program.getProgramNumber())
                .sequenceOrder(program.getSequenceOrder())
                .name(program.getName())
                .description(program.getDescription())
                .yearVersion(program.getYearVersion())
                .startDate(program.getStartDate())
                .status(program.getStatus())
                .totalInitiatives(initiatives.size())
                .initiatives(initiativeResponses)
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }

    private InitiativeResponse mapToInitiativeResponse(MstInitiative initiative) {
        return InitiativeResponse.builder()
                .id(initiative.getId())
                .programId(initiative.getProgram().getId())
                .initiativeNumber(initiative.getInitiativeNumber())
                .sequenceOrder(initiative.getSequenceOrder())
                .name(initiative.getName())
                .description(initiative.getDescription())
                .yearVersion(initiative.getYearVersion())
                .submitDate(initiative.getSubmitDate())
                .documentLink(initiative.getDocumentLink())
                .status(initiative.getStatus())
                .pksiRelationId(initiative.getPksiRelationId())
                .createdAt(initiative.getCreatedAt())
                .updatedAt(initiative.getUpdatedAt())
                .build();
    }
}
