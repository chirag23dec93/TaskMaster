package com.airtribe.TaskMaster.service;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.User;
import com.airtribe.TaskMaster.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository teamRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TeamService service;

    private Team team;
    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id("550e8400-e29b-41d4-a716-446655440000")
                .username("testuser")
                .email("test@example.com")
                .build();

        team = Team.builder()
                .id(UUID.fromString("550e8400-e29b-41d4-a716-446655440001"))
                .name("Test Team")
                .description("Test Description")
                .members(new HashSet<>())
                .build();
    }

    @Test
    void create_Success() {
        when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        Team result = service.create(team.getName(), team.getDescription(), user.getUsername());

        assertNotNull(result);
        assertEquals(team.getId(), result.getId());
        assertEquals(team.getName(), result.getName());
        assertEquals(team.getDescription(), result.getDescription());
        assertTrue(result.getMembers().contains(user));
    }

    @Test
    void get_Success() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        Team result = service.get(team.getId());

        assertNotNull(result);
        assertEquals(team.getId(), result.getId());
        assertEquals(team.getName(), result.getName());
        assertEquals(team.getDescription(), result.getDescription());
    }

    @Test
    void join_Success() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        Team result = service.join(team.getId(), user.getUsername());

        assertNotNull(result);
        assertTrue(result.getMembers().contains(user));
    }

    @Test
    void invite_Success() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));

        boolean result = service.invite(team.getId(), "test@example.com");

        assertTrue(result);
    }

    @Test
    void leave_Success() {
        team.getMembers().add(user);
        when(teamRepository.findById(team.getId())).thenReturn(Optional.of(team));
        when(userService.loadUserByUsername(user.getUsername())).thenReturn(user);
        when(teamRepository.save(any(Team.class))).thenReturn(team);

        Team result = service.leave(team.getId(), user.getUsername());

        assertNotNull(result);
        assertFalse(result.getMembers().contains(user));
    }

    @Test
    void get_NotFound() {
        when(teamRepository.findById(team.getId())).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> service.get(team.getId()));
    }
}
