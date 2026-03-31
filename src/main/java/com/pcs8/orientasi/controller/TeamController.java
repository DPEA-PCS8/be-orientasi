package com.pcs8.orientasi.controller;

import com.pcs8.orientasi.domain.dto.request.TeamRequest;
import com.pcs8.orientasi.domain.dto.response.BaseResponse;
import com.pcs8.orientasi.domain.dto.response.TeamMemberResponse;
import com.pcs8.orientasi.domain.dto.response.TeamResponse;
import com.pcs8.orientasi.service.TeamService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/teams")
@RequiredArgsConstructor
public class TeamController {

    private static final Logger logger = LoggerFactory.getLogger(TeamController.class);

    private final TeamService teamService;

    /**
     * Get all teams
     */
    @GetMapping
    public ResponseEntity<BaseResponse> getAllTeams() {
        logger.info("GET /api/teams - fetching all teams");
        List<TeamResponse> teams = teamService.getAllTeams();
        return ResponseEntity.ok(new BaseResponse(200, "Success", teams));
    }

    /**
     * Get team by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<BaseResponse> getTeamById(@PathVariable String id) {
        logger.info("GET /api/teams/{} - fetching team details", id);
        TeamResponse team = teamService.getTeamById(UUID.fromString(id));
        return ResponseEntity.ok(new BaseResponse(200, "Success", team));
    }

    /**
     * Create a new team
     */
    @PostMapping
    public ResponseEntity<BaseResponse> createTeam(@Valid @RequestBody TeamRequest request) {
        logger.info("POST /api/teams - creating new team: {}", request.getName());
        TeamResponse team = teamService.createTeam(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new BaseResponse(201, "Team created successfully", team));
    }

    /**
     * Update an existing team
     */
    @PutMapping("/{id}")
    public ResponseEntity<BaseResponse> updateTeam(
            @PathVariable String id,
            @Valid @RequestBody TeamRequest request) {
        logger.info("PUT /api/teams/{} - updating team", id);
        TeamResponse team = teamService.updateTeam(UUID.fromString(id), request);
        return ResponseEntity.ok(new BaseResponse(200, "Team updated successfully", team));
    }

    /**
     * Delete a team
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<BaseResponse> deleteTeam(@PathVariable String id) {
        logger.info("DELETE /api/teams/{} - deleting team", id);
        teamService.deleteTeam(UUID.fromString(id));
        return ResponseEntity.ok(new BaseResponse(200, "Team deleted successfully", null));
    }

    /**
     * Get all available users for team assignment
     */
    @GetMapping("/available-users")
    public ResponseEntity<BaseResponse> getAvailableUsers() {
        logger.info("GET /api/teams/available-users - fetching available users");
        List<TeamMemberResponse> users = teamService.getAvailableUsers();
        return ResponseEntity.ok(new BaseResponse(200, "Success", users));
    }
}
