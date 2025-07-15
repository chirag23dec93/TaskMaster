package com.airtribe.TaskMaster.service;


import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.TeamInvite;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.TeamInviteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamInviteService {
    private final TeamInviteRepository invites;
    @Autowired
    private  TeamService teamService;
    @Autowired
    private  UserService users;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public TeamInvite createInvite(UUID teamId, String email) {
        Team team = teamService.get(teamId);
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        TeamInvite invite = TeamInvite.builder()
                .email(email)
                .token(token)
                .createdAt(Instant.now())
                .accepted(false)
                .team(team)
                .build();
        return invites.save(invite);
    }

//    @Transactional
//    public void acceptInvite(String token, String username) {
//        TeamInvite invite = invites.findByToken(token)
//                .orElseThrow(() -> new RuntimeException("Invite not found"));
//        if (invite.isAccepted()) {
//            throw new RuntimeException("Invite already accepted");
//        }
//        User user = (User) users.loadUserByUsername(username);
//        Team team = invite.getTeam();
//        team.getMembers().add(user);
//        invite.setAccepted(true);        // mark the invite as accepted
//        invites.save(invite);
//    }

    @Transactional
    public void acceptInvite(String token, String username) {
        TeamInvite invite = invites.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invite not found"));
        if (invite.isAccepted()) {
            throw new RuntimeException("Invite already accepted");
        }

        // Ensure the currently-authenticated user *is* the invited email:
        User user = (User) users.loadUserByUsername(username);
        if (!user.getEmail().equalsIgnoreCase(invite.getEmail())) {
            throw new RuntimeException("Invite token does not belong to your account");
        }

        // Now it’s safe to join:
        Team team = invite.getTeam();
        team.getMembers().add(user);

        invite.setAccepted(true);
        invites.save(invite);
    }

}