package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.response.Fs2ChangelogResponse;
import com.pcs8.orientasi.service.Fs2ChangelogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/fs2")
@RequiredArgsConstructor
public class Fs2ChangelogController {

    private final Fs2ChangelogService fs2ChangelogService;

    /**
     * Get changelogs for an FS2 document
     * Retrieves all change history for a specific FS2 document
     */
    @GetMapping("/{fs2Id}/changelogs")
    public ResponseEntity<List<Fs2ChangelogResponse>> getChangelogs(@PathVariable UUID fs2Id) {
        List<Fs2ChangelogResponse> changelogs = fs2ChangelogService.getChangelogsByFs2Id(fs2Id);
        return ResponseEntity.ok(changelogs);
    }

    /**
     * Get changelog count for an FS2 document
     * Returns the total number of changes for a specific FS2 document
     */
    @GetMapping("/{fs2Id}/changelogs/count")
    public ResponseEntity<Long> getChangelogCount(@PathVariable UUID fs2Id) {
        long count = fs2ChangelogService.countChangelogsByFs2Id(fs2Id);
        return ResponseEntity.ok(count);
    }
}
