package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TeamService {
    private final TeamRepository teams;
    private final UserService users;

    @Transactional
    public Team create(String name, String description, String creatorUsername) {
        User creator = (User) users.loadUserByUsername(creatorUsername);
        Team t = Team.builder()
                .name(name)
                .description(description)
                .build();
        t.getMembers().add(creator);
        return teams.save(t);
    }

    @Transactional(readOnly = true)
    public Team get(UUID id) {
        return teams.findById(id)
                .orElseThrow(() -> new RuntimeException("Team not found: " + id));
    }

    @Transactional
    public void join(UUID teamId, String username) {
        Team t = get(teamId);
        User u = (User) users.loadUserByUsername(username);
        t.getMembers().add(u);
        teams.save(t);
    }
}