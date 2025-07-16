package com.airtribe.TaskMaster.controller;

import com.airtribe.TaskMaster.config.TestConfig;
import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.TeamInvite;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.service.TeamInviteService;
import com.airtribe.TaskMaster.service.TeamService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;

@WebMvcTest(TeamController.class)
@Import(TestConfig.class)
@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamService teamService;

    @Mock
    private TeamInviteService teamInviteService;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ObjectMapper objectMapper;

    private Team team;
    private User user;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(springSecurity())
                .build();

        user = new User();
        user.setId("user123");
        user.setUsername("testuser");

        team = Team.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .name("Test Team")
                .build();
    }

    @Test
    @WithMockUser(username = "testuser")
    void createTeam_Success() throws Exception {
        Team request = Team.builder()
                .name("Test Team")
                .build();

        when(teamService.create(eq("Test Team"), any(), eq("testuser")))
                .thenReturn(team);

        mockMvc.perform(post("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Team"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void getTeam_Success() throws Exception {
        when(teamService.get(team.getId())).thenReturn(team);

        mockMvc.perform(get("/api/teams/{id}", team.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(team.getId().toString()))
                .andExpect(jsonPath("$.name").value("Test Team"));
    }

    @Test
    @WithMockUser(username = "testuser")
    void joinTeam_Success() throws Exception {
        mockMvc.perform(post("/api/teams/{id}/join", team.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "testuser")
    void inviteMember_Success() throws Exception {
        String email = "invite@example.com";
        TeamInvite invite = TeamInvite.builder()
                .id(UUID.randomUUID())
                .team(team)
                .email(email)
                .token(UUID.randomUUID().toString())
                .createdAt(Instant.now())
                .build();
        when(teamInviteService.createInvite(team.getId(), email)).thenReturn(invite);

        mockMvc.perform(post("/api/teams/{id}/invite", team.getId())
                .param("email", email)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(invite.getId().toString()));
    }

    @Test
    @WithMockUser(username = "testuser")
    void acceptInvite_Success() throws Exception {
        String token = "test-token";

        mockMvc.perform(post("/api/teams/invites/{token}/accept", token)
                .contentType(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createTeam_Unauthorized() throws Exception {
        Team request = Team.builder()
                .name("Test Team")
                .build();

        mockMvc.perform(post("/api/teams")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }
}
