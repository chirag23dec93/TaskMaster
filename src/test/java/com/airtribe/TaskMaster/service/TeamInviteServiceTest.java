package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.TeamInvite;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.TeamInviteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TeamInviteServiceTest {

    @Mock
    private TeamInviteRepository inviteRepository;

    @Mock
    private TeamService teamService;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeamInviteService service;

    private Team team;
    private User user;
    private TeamInvite invite;

    @BeforeEach
    void setUp() {
        team = Team.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"))
                .name("Test Team")
                .members(Set.of())
                .build();

        user = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440001")
                .username("testuser")
                .email("test@example.com")
                .build();

        invite = TeamInvite.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440002"))
                .email("test@example.com")
                .token("test-token")
                .createdAt(Instant.now())
                .accepted(false)
                .team(team)
                .build();
    }

    @Test
    void createInvite_Success() {
        String email = "test@example.com";
        when(teamService.get(team.getId())).thenReturn(team);
        when(inviteRepository.save(any(TeamInvite.class))).thenReturn(invite);

        TeamInvite result = service.createInvite(team.getId(), email);

        assertNotNull(result);
        assertEquals(invite.getId(), result.getId());
        assertEquals(email, result.getEmail());
        assertNotNull(result.getToken());
        assertNotNull(result.getCreatedAt());
        assertFalse(result.isAccepted());
        assertEquals(team.getId(), result.getTeam().getId());
    }

    @Test
    void acceptInvite_Success() {
        when(inviteRepository.findByToken(invite.getToken())).thenReturn(Optional.of(invite));
        when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(inviteRepository.save(any(TeamInvite.class))).thenReturn(invite);

        assertDoesNotThrow(() -> service.acceptInvite(invite.getToken(), user.getUsername()));

        assertTrue(invite.isAccepted());
        assertTrue(team.getMembers().contains(user));
    }

    @Test
    void acceptInvite_AlreadyAccepted() {
        invite.setAccepted(true);
        when(inviteRepository.findByToken(invite.getToken())).thenReturn(Optional.of(invite));

        assertThrows(RuntimeException.class, () -> service.acceptInvite(invite.getToken(), user.getUsername()));
    }

    @Test
    void acceptInvite_WrongEmail() {
        User wrongUser = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440003")
                .username("wronguser")
                .email("wrong@example.com")
                .build();

        when(inviteRepository.findByToken(invite.getToken())).thenReturn(Optional.of(invite));
        when(userService.loadUserByUsername(wrongUser.getUsername())).thenReturn(wrongUser);

        assertThrows(RuntimeException.class, () -> service.acceptInvite(invite.getToken(), wrongUser.getUsername()));
    }
}
