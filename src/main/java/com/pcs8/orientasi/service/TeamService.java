package com.pcs8.orientasi.service;

import com.pcs8.orientasi.domain.dto.request.TeamRequest;
import com.pcs8.orientasi.domain.dto.response.TeamMemberResponse;
import com.pcs8.orientasi.domain.dto.response.TeamResponse;
import com.pcs8.orientasi.domain.entity.MstTeam;
import com.pcs8.orientasi.domain.entity.MstUser;
import com.pcs8.orientasi.exception.BadRequestException;
import com.pcs8.orientasi.exception.ResourceNotFoundException;
import com.pcs8.orientasi.repository.MstUserRepository;
import com.pcs8.orientasi.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {

    private static final Logger logger = LoggerFactory.getLogger(TeamService.class);

    private final TeamRepository teamRepository;
    private final MstUserRepository userRepository;

    /**
     * Get all teams with full details
     */
    @Transactional(readOnly = true)
    public List<TeamResponse> getAllTeams() {
        logger.info("Fetching all teams with details");
        List<MstTeam> teams = teamRepository.findAllWithDetails();
        return teams.stream()
                .map(this::mapToResponse)
                .toList();
    }

    /**
     * Get team by ID
     */
    @Transactional(readOnly = true)
    public TeamResponse getTeamById(UUID id) {
        logger.info("Fetching team with ID: {}", id);
        MstTeam team = teamRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
        return mapToResponse(team);
    }

    /**
     * Create a new team
     */
    @Transactional
    public TeamResponse createTeam(TeamRequest request) {
        logger.info("Creating new team");

        // Validate team name uniqueness
        if (teamRepository.existsByNameIgnoreCase(request.getName())) {
            throw new BadRequestException("Team with name '" + request.getName() + "' already exists");
        }

        MstTeam team = MstTeam.builder()
                .name(request.getName())
                .description(request.getDescription())
                .build();

        // Set PIC if provided
        if (request.getPicUuid() != null && !request.getPicUuid().isEmpty()) {
            MstUser pic = findUserByUuid(request.getPicUuid());
            team.setPic(pic);
        }

        // Save team first to get ID
        team = teamRepository.save(team);

        // Add members if provided
        if (request.getMemberUuids() != null && !request.getMemberUuids().isEmpty()) {
            for (String memberUuid : request.getMemberUuids()) {
                MstUser member = findUserByUuid(memberUuid);
                team.addMember(member);
            }
            team = teamRepository.save(team);
        }

        logger.info("Team created successfully with ID: {}", team.getId());
        return mapToResponse(team);
    }

    /**
     * Update an existing team
     */
    @Transactional
    public TeamResponse updateTeam(UUID id, TeamRequest request) {
        logger.info("Updating team with ID: {}", id);

        MstTeam team = teamRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));

        // Validate team name uniqueness (excluding current team)
        if (request.getName() != null && !request.getName().equals(team.getName())) {
            if (teamRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new BadRequestException("Team with name '" + request.getName() + "' already exists");
            }
            team.setName(request.getName());
        }

        // Update description
        if (request.getDescription() != null) {
            team.setDescription(request.getDescription());
        }

        // Update PIC
        if (request.getPicUuid() != null) {
            if (request.getPicUuid().isEmpty()) {
                team.setPic(null);
            } else {
                MstUser pic = findUserByUuid(request.getPicUuid());
                team.setPic(pic);
            }
        }

        // Update members if provided
        if (request.getMemberUuids() != null) {
            // Clear existing members and flush to avoid unique constraint violation
            team.clearMembers();
            teamRepository.saveAndFlush(team);
            
            // Re-fetch team to ensure clean state after flush
            team = teamRepository.findByIdWithDetails(id)
                    .orElseThrow(() -> new ResourceNotFoundException("Team not found with ID: " + id));
            
            // Add new members
            for (String memberUuid : request.getMemberUuids()) {
                MstUser member = findUserByUuid(memberUuid);
                team.addMember(member);
            }
        }

        team = teamRepository.save(team);
        logger.info("Team updated successfully: {}", team.getId());
        return mapToResponse(team);
    }

    /**
     * Delete a team
     */
    @Transactional
    public void deleteTeam(UUID id) {
        logger.info("Deleting team with ID: {}", id);
        
        if (!teamRepository.existsById(id)) {
            throw new ResourceNotFoundException("Team not found with ID: " + id);
        }
        
        teamRepository.deleteById(id);
        logger.info("Team deleted successfully: {}", id);
    }

    /**
     * Get all available users for team assignment
     */
    @Transactional(readOnly = true)
    public List<TeamMemberResponse> getAvailableUsers() {
        logger.info("Fetching all available users for team assignment");
        List<MstUser> users = userRepository.findAll();
        return users.stream()
                .map(this::mapToMemberResponse)
                .toList();
    }

    /**
     * Find user by UUID string
     */
    private MstUser findUserByUuid(String uuidString) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidString);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid UUID format: " + uuidString);
        }
        
        return userRepository.findById(uuid)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with UUID: " + uuidString));
    }

    /**
     * Map MstTeam entity to TeamResponse DTO
     */
    private TeamResponse mapToResponse(MstTeam team) {
        List<TeamMemberResponse> members = new ArrayList<>();
        
        if (team.getTeamMembers() != null) {
            members = team.getTeamMembers().stream()
                    .map(tm -> mapToMemberResponse(tm.getUser()))
                    .toList();
        }

        return TeamResponse.builder()
                .id(team.getId().toString())
                .name(team.getName())
                .description(team.getDescription())
                .pic(team.getPic() != null ? mapToMemberResponse(team.getPic()) : null)
                .members(members)
                .createdAt(team.getCreatedAt())
                .updatedAt(team.getUpdatedAt())
                .build();
    }

    /**
     * Map MstUser entity to TeamMemberResponse DTO
     */
    private TeamMemberResponse mapToMemberResponse(MstUser user) {
        return TeamMemberResponse.builder()
                .uuid(user.getUuid().toString())
                .username(user.getUsername())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .department(user.getDepartment())
                .title(user.getTitle())
                .build();
    }
}
