package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.InitiativeCreateRequest;
import com.pcs8.orientasi.domain.dto.response.InitiativeResponse;
import com.pcs8.orientasi.domain.entity.MstInitiative;
import com.pcs8.orientasi.domain.entity.MstProgram;
import com.pcs8.orientasi.repository.InitiativeRepository;
import com.pcs8.orientasi.repository.ProgramRepository;
import com.pcs8.orientasi.service.InitiativeService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class InitiativeServiceImpl implements InitiativeService {

    private static final Logger log = LoggerFactory.getLogger(InitiativeServiceImpl.class);

    private final InitiativeRepository initiativeRepository;
    private final ProgramRepository programRepository;

    @Override
    @Transactional
    public InitiativeResponse create(InitiativeCreateRequest request) {
        log.info("Creating new Initiative for Program: {} with year version: {}",
                request.getProgramId(), request.getYearVersion());

        MstProgram program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new IllegalArgumentException("Program not found with id: " + request.getProgramId()));

        Integer sortOrder;
        if (request.getInsertAtPosition() != null && request.getInsertAtPosition() > 0) {
            sortOrder = request.getInsertAtPosition();
            initiativeRepository.incrementSortOrderFrom(
                    request.getProgramId(),
                    request.getYearVersion(),
                    sortOrder);
        } else {
            sortOrder = initiativeRepository.findMaxSortOrder(request.getProgramId(), request.getYearVersion()) + 1;
        }

        String initiativeNumber = program.getProgramNumber() + "." + sortOrder;

        MstInitiative initiative = MstInitiative.builder()
                .program(program)
                .initiativeNumber(initiativeNumber)
                .name(request.getName())
                .description(request.getDescription())
                .yearVersion(request.getYearVersion())
                .sortOrder(sortOrder)
                .status(request.getStatus() != null ? request.getStatus() : "pending")
                .linkDokumen(request.getLinkDokumen())
                .tanggalSubmit(parseDateTime(request.getTanggalSubmit()))
                .build();

        MstInitiative saved = initiativeRepository.save(initiative);

        renumberAllInitiatives(request.getProgramId(), request.getYearVersion(), program.getProgramNumber());

        log.info("Initiative created successfully with id: {} and number: {}",
                saved.getId(), initiativeNumber);

        return mapToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InitiativeResponse> findByProgramAndYear(UUID programId, Integer yearVersion) {
        log.debug("Fetching initiatives for Program: {} with year version: {}", programId, yearVersion);
        return initiativeRepository.findByProgramIdAndYearVersion(programId, yearVersion).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public InitiativeResponse findById(UUID id) {
        log.debug("Fetching initiative by id: {}", id);
        MstInitiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Initiative not found with id: " + id));
        return mapToResponse(initiative);
    }

    @Override
    @Transactional
    public InitiativeResponse update(UUID id, InitiativeCreateRequest request) {
        log.info("Updating initiative with id: {}", id);

        MstInitiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Initiative not found with id: " + id));

        initiative.setName(request.getName());
        initiative.setDescription(request.getDescription());
        initiative.setStatus(request.getStatus() != null ? request.getStatus() : initiative.getStatus());
        initiative.setLinkDokumen(request.getLinkDokumen());
        initiative.setTanggalSubmit(parseDateTime(request.getTanggalSubmit()));

        MstInitiative saved = initiativeRepository.save(initiative);
        log.info("Initiative updated successfully");

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Deleting initiative with id: {}", id);

        MstInitiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Initiative not found with id: " + id));

        UUID programId = initiative.getProgram().getId();
        Integer yearVersion = initiative.getYearVersion();
        String programNumber = initiative.getProgram().getProgramNumber();

        initiativeRepository.delete(initiative);

        renumberAllInitiatives(programId, yearVersion, programNumber);

        log.info("Initiative deleted and numbers renumbered successfully");
    }

    @Override
    @Transactional
    public InitiativeResponse updateStatus(UUID id, String status) {
        log.info("Updating status for initiative: {} to: {}", id, status);

        MstInitiative initiative = initiativeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Initiative not found with id: " + id));

        initiative.setStatus(status);
        MstInitiative saved = initiativeRepository.save(initiative);

        log.info("Initiative status updated successfully");
        return mapToResponse(saved);
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

    private InitiativeResponse mapToResponse(MstInitiative initiative) {
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
