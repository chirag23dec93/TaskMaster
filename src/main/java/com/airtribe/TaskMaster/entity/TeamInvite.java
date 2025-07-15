package com.airtribe.TaskMaster.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "team_invites")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeamInvite {
    @Id
    @GeneratedValue
    private UUID id;

    private String email;
    private String token;
    private Instant createdAt;
    private boolean accepted;

    @ManyToOne(optional = false)
    private Team team;
}