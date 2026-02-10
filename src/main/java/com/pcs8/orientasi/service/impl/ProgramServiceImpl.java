package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.ProgramRequest;
import com.pcs8.orientasi.domain.dto.response.InitiativeResponse;
import com.pcs8.orientasi.domain.dto.response.ProgramListResponse;
import com.pcs8.orientasi.domain.dto.response.ProgramResponse;
import com.pcs8.orientasi.domain.entity.MstInitiative;
import com.pcs8.orientasi.domain.entity.MstProgram;
import com.pcs8.orientasi.domain.entity.MstRbsi;
import com.pcs8.orientasi.repository.InitiativeRepository;
import com.pcs8.orientasi.repository.ProgramRepository;
import com.pcs8.orientasi.repository.RbsiRepository;
import com.pcs8.orientasi.service.ProgramService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProgramServiceImpl implements ProgramService {

    private static final Logger log = LoggerFactory.getLogger(ProgramServiceImpl.class);
    private static final String PROGRAM_NUMBER_PREFIX = "3.";

    private final ProgramRepository programRepository;
    private final RbsiRepository rbsiRepository;
    private final InitiativeRepository initiativeRepository;

    @Override
    @Transactional
    public ProgramResponse create(ProgramRequest request) {
        log.info("Creating new Program for RBSI: {} with year version: {}", 
                request.getRbsiId(), request.getYearVersion());

        MstRbsi rbsi = rbsiRepository.findById(request.getRbsiId())
                .orElseThrow(() -> new IllegalArgumentException("RBSI not found with id: " + request.getRbsiId()));

        Integer sortOrder;
        if (request.getInsertAtPosition() != null && request.getInsertAtPosition() > 0) {
            sortOrder = request.getInsertAtPosition();
            programRepository.incrementSortOrderFrom(
                    request.getRbsiId(), 
                    request.getYearVersion(), 
                    sortOrder);
            renumberAllPrograms(request.getRbsiId(), request.getYearVersion());
        } else {
            sortOrder = programRepository.findMaxSortOrder(request.getRbsiId(), request.getYearVersion()) + 1;
        }

        String programNumber = PROGRAM_NUMBER_PREFIX + sortOrder;

        MstProgram program = MstProgram.builder()
                .rbsi(rbsi)
                .programNumber(programNumber)
                .name(request.getName())
                .description(request.getDescription())
                .yearVersion(request.getYearVersion())
                .sortOrder(sortOrder)
                .status(request.getStatus() != null ? request.getStatus() : "active")
                .startDate(parseDateTime(request.getStartDate()))
                .build();

        MstProgram savedProgram = programRepository.save(program);

        if (request.getInitiatives() != null && !request.getInitiatives().isEmpty()) {
            createInitiatives(savedProgram, request.getInitiatives(), request.getYearVersion());
        }

        renumberAllInitiatives(savedProgram.getId(), request.getYearVersion(), programNumber);

        log.info("Program created successfully with id: {} and number: {}", 
                savedProgram.getId(), programNumber);

        return mapToResponse(savedProgram, true);
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramListResponse findByRbsiAndYear(UUID rbsiId, Integer yearVersion) {
        log.debug("Fetching programs for RBSI: {} with year version: {}", rbsiId, yearVersion);

        MstRbsi rbsi = rbsiRepository.findById(rbsiId)
                .orElseThrow(() -> new IllegalArgumentException("RBSI not found with id: " + rbsiId));

        List<MstProgram> programs = programRepository.findByRbsiIdAndYearVersionWithInitiatives(
                rbsiId, yearVersion);

        List<ProgramResponse> programResponses = programs.stream()
                .map(p -> mapToResponse(p, true))
                .collect(Collectors.toList());

        return ProgramListResponse.builder()
                .rbsiId(rbsiId)
                .periode(rbsi.getPeriode())
                .yearVersion(yearVersion)
                .totalPrograms(programResponses.size())
                .programs(programResponses)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ProgramResponse findById(UUID id) {
        log.debug("Fetching program by id: {}", id);
        MstProgram program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program not found with id: " + id));
        return mapToResponse(program, true);
    }

    @Override
    @Transactional
    public ProgramResponse update(UUID id, ProgramRequest request) {
        log.info("Updating program with id: {}", id);

        MstProgram program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program not found with id: " + id));

        program.setName(request.getName());
        program.setDescription(request.getDescription());
        program.setStatus(request.getStatus() != null ? request.getStatus() : program.getStatus());
        program.setStartDate(parseDateTime(request.getStartDate()));

        MstProgram savedProgram = programRepository.save(program);
        log.info("Program updated successfully");

        return mapToResponse(savedProgram, true);
    }

    @Override
    @Transactional
    public void delete(UUID id, Integer yearVersion) {
        log.info("Deleting program with id: {} for year: {}", id, yearVersion);

        MstProgram program = programRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Program not found with id: " + id));

        UUID rbsiId = program.getRbsi().getId();
        
        programRepository.delete(program);

        renumberAllPrograms(rbsiId, yearVersion);

        log.info("Program deleted and numbers renumbered successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public List<Integer> getAvailableYears(UUID rbsiId) {
        log.debug("Fetching available years for RBSI: {}", rbsiId);
        return programRepository.findDistinctYearVersionsByRbsiId(rbsiId);
    }

    private void createInitiatives(MstProgram program, List<ProgramRequest.InitiativeRequest> initiativeRequests, 
                                   Integer yearVersion) {
        int sortOrder = 1;
        for (ProgramRequest.InitiativeRequest req : initiativeRequests) {
            MstInitiative initiative = MstInitiative.builder()
                    .program(program)
                    .initiativeNumber(program.getProgramNumber() + "." + sortOrder)
                    .name(req.getName())
                    .description(req.getDescription())
                    .yearVersion(yearVersion)
                    .sortOrder(sortOrder)
                    .status(req.getStatus() != null ? req.getStatus() : "pending")
                    .linkDokumen(req.getLinkDokumen())
                    .tanggalSubmit(parseDateTime(req.getTanggalSubmit()))
                    .build();

            initiativeRepository.save(initiative);
            sortOrder++;
        }
    }

    private void renumberAllPrograms(UUID rbsiId, Integer yearVersion) {
        List<MstProgram> programs = programRepository.findByRbsiIdAndYearVersion(rbsiId, yearVersion);
        int newSortOrder = 1;
        for (MstProgram program : programs) {
            String newProgramNumber = PROGRAM_NUMBER_PREFIX + newSortOrder;
            program.setSortOrder(newSortOrder);
            program.setProgramNumber(newProgramNumber);
            programRepository.save(program);

            renumberAllInitiatives(program.getId(), yearVersion, newProgramNumber);
            newSortOrder++;
        }
    }

    private void renumberAllInitiatives(UUID programId, Integer yearVersion, String programNumber) {
        List<MstInitiative> initiatives = initiativeRepository.findByProgramIdAndYearVersion(programId, yearVersion);
        int sortOrder = 1;
        for (MstInitiative initiative : initiatives) {
            initiative.setSortOrder(sortOrder);
            initiative.setInitiativeNumber(programNumber + "." + sortOrder);
            initiativeRepository.save(initiative);
            sortOrder++;
        }
    }

    private LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.isEmpty()) {
            return null;
        }
        try {
            if (dateTimeStr.length() == 10) {
                return LocalDateTime.parse(dateTimeStr + "T00:00:00");
            }
            return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e) {
            log.warn("Failed to parse datetime: {}", dateTimeStr);
            return null;
        }
    }

    private ProgramResponse mapToResponse(MstProgram program, boolean includeInitiatives) {
        List<InitiativeResponse> initiativeResponses = new ArrayList<>();
        
        if (includeInitiatives && program.getInitiatives() != null) {
            initiativeResponses = program.getInitiatives().stream()
                    .filter(i -> i.getYearVersion().equals(program.getYearVersion()))
                    .sorted((a, b) -> a.getSortOrder().compareTo(b.getSortOrder()))
                    .map(this::mapInitiativeToResponse)
                    .collect(Collectors.toList());
        }

        return ProgramResponse.builder()
                .id(program.getId())
                .rbsiId(program.getRbsi().getId())
                .programNumber(program.getProgramNumber())
                .name(program.getName())
                .description(program.getDescription())
                .yearVersion(program.getYearVersion())
                .sortOrder(program.getSortOrder())
                .status(program.getStatus())
                .startDate(program.getStartDate())
                .totalInitiatives(initiativeResponses.size())
                .initiatives(initiativeResponses)
                .createdAt(program.getCreatedAt())
                .updatedAt(program.getUpdatedAt())
                .build();
    }

    private InitiativeResponse mapInitiativeToResponse(MstInitiative initiative) {
        return InitiativeResponse.builder()
                .id(initiative.getId())
                .programId(initiative.getProgram().getId())
                .initiativeNumber(initiative.getInitiativeNumber())
                .name(initiative.getName())
                .description(initiative.getDescription())
                .yearVersion(initiative.getYearVersion())
                .sortOrder(initiative.getSortOrder())
                .status(initiative.getStatus())
                .linkDokumen(initiative.getLinkDokumen())
                .tanggalSubmit(initiative.getTanggalSubmit())
                .pksiRelationId(initiative.getPksiRelationId())
                .createdAt(initiative.getCreatedAt())
                .updatedAt(initiative.getUpdatedAt())
                .build();
    }
}
