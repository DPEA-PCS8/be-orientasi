package com.pcs8.orientasi.service;

import com.pcs8.orientasi.config.UserContext;
import com.pcs8.orientasi.domain.dto.response.MyWorkResponse;
import com.pcs8.orientasi.domain.entity.Fs2Document;
import com.pcs8.orientasi.domain.entity.MstTeam;
import com.pcs8.orientasi.domain.entity.PksiDocument;
import com.pcs8.orientasi.repository.Fs2DocumentRepository;
import com.pcs8.orientasi.repository.PksiDocumentRepository;
import com.pcs8.orientasi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MyWorkService {

    private final TeamRepository teamRepository;
    private final PksiDocumentRepository pksiRepository;
    private final Fs2DocumentRepository fs2Repository;
    private final UserContext userContext;

    @Transactional(readOnly = true)
    public MyWorkResponse getMyWork() {
        UUID userId = userContext.getCurrentUserId();

        List<MstTeam> teams = teamRepository.findByMemberUuid(userId);
        List<UUID> teamIds = teams.stream().map(MstTeam::getId).toList();

        List<PksiDocument> pksiList = teamIds.isEmpty() ? List.of() : pksiRepository.findActiveByTeamIds(teamIds);
        List<Fs2Document> fs2List = teamIds.isEmpty() ? List.of() : fs2Repository.findActiveByTeamIds(teamIds);

        return MyWorkResponse.builder()
                .teams(teams.stream().map(t -> MyWorkResponse.TeamSummary.builder()
                        .id(t.getId().toString())
                        .name(t.getName())
                        .build()).toList())
                .pksiList(pksiList.stream().map(this::toPksiCard).toList())
                .fs2List(fs2List.stream().map(this::toFs2Card).toList())
                .build();
    }

    private MyWorkResponse.PksiCardItem toPksiCard(PksiDocument p) {
        return MyWorkResponse.PksiCardItem.builder()
                .id(p.getId().toString())
                .namaPksi(p.getNamaPksi())
                .namaAplikasi(p.getAplikasi() != null ? p.getAplikasi().getNamaAplikasi() : null)
                .status(p.getStatus() != null ? p.getStatus().name() : null)
                .progress(p.getProgress())
                .targetGoLive(p.getEffectiveTargetGoLiveDate())
                .teamName(p.getTeam() != null ? p.getTeam().getName() : null)
                .jenisPksi(p.getJenisPksi())
                .build();
    }

    private MyWorkResponse.Fs2CardItem toFs2Card(Fs2Document f) {
        return MyWorkResponse.Fs2CardItem.builder()
                .id(f.getId().toString())
                .namaFs2(f.getNamaFs2())
                .namaAplikasi(f.getAplikasi() != null ? f.getAplikasi().getNamaAplikasi() : null)
                .status(f.getStatus())
                .progres(f.getProgres())
                .progresStatus(f.getProgresStatus())
                .targetGoLive(f.getTargetGoLive())
                .teamName(f.getTeam() != null ? f.getTeam().getName() : null)
                .fasePengajuan(f.getFasePengajuan())
                .build();
    }
}
