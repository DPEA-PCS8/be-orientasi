package com.pcs8.orientasi.service.impl;

import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifItemRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiInisiatifRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiProgramRequest;
import com.pcs8.orientasi.domain.dto.request.RbsiRequest;
import com.pcs8.orientasi.domain.dto.response.RbsiHistoryResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiInisiatifResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiProgramResponse;
import com.pcs8.orientasi.domain.dto.response.RbsiResponse;
import com.pcs8.orientasi.domain.entity.Rbsi;
import com.pcs8.orientasi.domain.entity.RbsiInisiatif;
import com.pcs8.orientasi.domain.entity.RbsiProgram;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.RbsiInisiatifRepository;
import com.pcs8.orientasi.repository.RbsiProgramRepository;
import com.pcs8.orientasi.repository.RbsiRepository;
import com.pcs8.orientasi.service.RbsiService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RbsiServiceImpl implements RbsiService {

    private static final Logger log = LoggerFactory.getLogger(RbsiServiceImpl.class);

    private final RbsiRepository rbsiRepository;
    private final RbsiProgramRepository programRepository;
    private final RbsiInisiatifRepository inisiatifRepository;

    @Override
    @Transactional
    public RbsiResponse createRbsi(RbsiRequest request) {
        if (rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new BadRequestException("RBSI dengan periode ini sudah ada");
        }

        Rbsi rbsi = Rbsi.builder()
                .periode(request.getPeriode())
                .build();

        Rbsi saved = rbsiRepository.save(rbsi);
        log.info("RBSI created: {}", saved.getId());
        return mapToRbsiResponse(saved, null);
    }

    @Override
    @Transactional(readOnly = true)
    public RbsiResponse getRbsi(UUID id, Integer tahun) {
        Rbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        Integer resolvedTahun = resolveTahun(rbsi.getId(), tahun);
        List<RbsiProgramResponse> programs = null;
        if (resolvedTahun != null) {
            programs = getProgramsByRbsiAndTahun(rbsi.getId(), resolvedTahun);
        }

        return mapToRbsiResponse(rbsi, programs);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RbsiResponse> getAllRbsi() {
        return rbsiRepository.findAll().stream()
                .map(rbsi -> mapToRbsiResponse(rbsi, null))
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public RbsiResponse updateRbsi(UUID id, RbsiRequest request) {
        Rbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        if (!rbsi.getPeriode().equals(request.getPeriode()) && rbsiRepository.existsByPeriode(request.getPeriode())) {
            throw new BadRequestException("RBSI dengan periode ini sudah ada");
        }

        rbsi.setPeriode(request.getPeriode());
        Rbsi saved = rbsiRepository.save(rbsi);
        log.info("RBSI updated: {}", saved.getId());
        return mapToRbsiResponse(saved, null);
    }

    @Override
    @Transactional
    public void deleteRbsi(UUID id) {
        Rbsi rbsi = rbsiRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));
        rbsiRepository.delete(rbsi);
        log.info("RBSI deleted: {}", id);
    }

    @Override
    @Transactional
    public RbsiProgramResponse createOrUpdateProgram(RbsiProgramRequest request) {
        Rbsi rbsi = rbsiRepository.findById(request.getRbsiId())
                .orElseThrow(() -> new ResourceNotFoundException("RBSI tidak ditemukan"));

        RbsiProgram program = programRepository
                .findByRbsiIdAndTahunAndNomorProgram(request.getRbsiId(), request.getTahun(), request.getNomorProgram())
                .orElseGet(() -> RbsiProgram.builder()
                        .rbsi(rbsi)
                        .tahun(request.getTahun())
                        .nomorProgram(request.getNomorProgram())
                        .build());

        program.setNamaProgram(request.getNamaProgram());
        RbsiProgram savedProgram = programRepository.save(program);

        if (request.getInisiatifs() != null) {
            upsertInisiatifs(savedProgram, request.getInisiatifs());
        }

        log.info("Program saved: {}", savedProgram.getId());
        return mapToProgramResponse(savedProgram, true);
    }

    @Override
    @Transactional
    public RbsiProgramResponse updateProgram(UUID programId, RbsiProgramRequest request) {
        RbsiProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));

        if (!program.getRbsi().getId().equals(request.getRbsiId())) {
            throw new BadRequestException("Rbsi id tidak sesuai dengan program");
        }

        if (!program.getNomorProgram().equals(request.getNomorProgram())
                || !program.getTahun().equals(request.getTahun())) {
            boolean exists = programRepository.existsByRbsiIdAndTahunAndNomorProgram(
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
                    .findByProgramIdAndTahunOrderByNomorInisiatifAsc(savedProgram.getId(), previousTahun);
            for (RbsiInisiatif inisiatif : inisiatifs) {
                inisiatif.setTahun(request.getTahun());
                inisiatifRepository.save(inisiatif);
            }
        }

        if (request.getInisiatifs() != null) {
            upsertInisiatifs(savedProgram, request.getInisiatifs());
        }

        log.info("Program updated: {}", savedProgram.getId());
        return mapToProgramResponse(savedProgram, true);
    }

    @Override
    @Transactional
    public void deleteProgram(UUID programId) {
        RbsiProgram program = programRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));
        programRepository.delete(program);
        log.info("Program deleted: {}", programId);
    }

    @Override
    @Transactional
    public RbsiInisiatifResponse createOrUpdateInisiatif(RbsiInisiatifRequest request) {
        RbsiProgram program = programRepository.findById(request.getProgramId())
                .orElseThrow(() -> new ResourceNotFoundException("Program tidak ditemukan"));

        if (!program.getTahun().equals(request.getTahun())) {
            throw new BadRequestException("Tahun inisiatif harus sama dengan tahun program");
        }

        RbsiInisiatif inisiatif = inisiatifRepository
                .findByProgramIdAndTahunAndNomorInisiatif(request.getProgramId(), request.getTahun(), request.getNomorInisiatif())
                .orElseGet(() -> RbsiInisiatif.builder()
                        .program(program)
                        .tahun(request.getTahun())
                        .nomorInisiatif(request.getNomorInisiatif())
                        .build());

        inisiatif.setNamaInisiatif(request.getNamaInisiatif());

        RbsiInisiatif savedInisiatif = inisiatifRepository.save(inisiatif);
        log.info("Inisiatif saved: {}", savedInisiatif.getId());
        return mapToInisiatifResponse(savedInisiatif);
    }

    @Override
    @Transactional
    public RbsiInisiatifResponse updateInisiatif(UUID inisiatifId, RbsiInisiatifRequest request) {
        RbsiInisiatif inisiatif = inisiatifRepository.findById(inisiatifId)
                .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));

        if (!inisiatif.getProgram().getId().equals(request.getProgramId())) {
            throw new BadRequestException("Program id tidak sesuai dengan inisiatif");
        }

        if (!inisiatif.getNomorInisiatif().equals(request.getNomorInisiatif())
                || !inisiatif.getTahun().equals(request.getTahun())) {
            boolean exists = inisiatifRepository.existsByProgramIdAndTahunAndNomorInisiatif(
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

        RbsiInisiatif savedInisiatif = inisiatifRepository.save(inisiatif);
        log.info("Inisiatif updated: {}", savedInisiatif.getId());
        return mapToInisiatifResponse(savedInisiatif);
    }

    @Override
    @Transactional
    public void deleteInisiatif(UUID inisiatifId) {
        RbsiInisiatif inisiatif = inisiatifRepository.findById(inisiatifId)
                .orElseThrow(() -> new ResourceNotFoundException("Inisiatif tidak ditemukan"));
        inisiatifRepository.delete(inisiatif);
        log.info("Inisiatif deleted: {}", inisiatifId);
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

    private Integer resolveTahun(UUID rbsiId, Integer tahun) {
        if (tahun != null) {
            return tahun;
        }
        return programRepository.findMaxTahunByRbsiId(rbsiId);
    }

    private void upsertInisiatifs(RbsiProgram program, List<RbsiInisiatifItemRequest> inisiatifs) {
        Map<String, RbsiInisiatif> existing = inisiatifRepository
            .findByProgramIdAndTahunOrderByNomorInisiatifAsc(program.getId(), program.getTahun()).stream()
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
                    .findByProgramIdAndTahunOrderByNomorInisiatifAsc(program.getId(), program.getTahun())
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
                .programId(inisiatif.getProgram().getId())
                .tahun(inisiatif.getTahun())
                .nomorInisiatif(inisiatif.getNomorInisiatif())
                .namaInisiatif(inisiatif.getNamaInisiatif())
                .createdAt(inisiatif.getCreatedAt())
                .updatedAt(inisiatif.getUpdatedAt())
                .build();
    }
}
