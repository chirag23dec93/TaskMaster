package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.TeamInvite;
import com.airtribe.TaskMaster.service.TeamInviteService;
import com.airtribe.TaskMaster.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.UUID;

@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private final TeamService svc;

    private final TeamInviteService teamInviteService;

    @PostMapping
    public ResponseEntity<Team> create(
            @AuthenticationPrincipal UserDetails user,
            @RequestBody Team dto
    ) {
        Team t = svc.create(dto.getName(), dto.getDescription(), user.getUsername());
        return ResponseEntity.status(201).body(t);
    }

    @GetMapping("/{id}")
    public Team get(@PathVariable UUID id) {
        return svc.get(id);
    }

    @PostMapping("/{id}/join")
    public ResponseEntity<Void> join(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails user
    ) {
        svc.join(id, user.getUsername());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/invite")
    public ResponseEntity<TeamInvite> invite(
            @PathVariable UUID id,
            @RequestParam String email
    ) {
        TeamInvite inv = teamInviteService.createInvite(id, email);
        return ResponseEntity.status(201).body(inv);
    }

    @PostMapping("/invites/{token}/accept")
    public ResponseEntity<Void> acceptInvite(
            @PathVariable String token,
            @AuthenticationPrincipal UserDetails user
    ) {
        teamInviteService.acceptInvite(token, user.getUsername());
        return ResponseEntity.noContent().build();
    }
}
