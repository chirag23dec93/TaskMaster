package com.airtribe.TaskMaster.repository;
import com.airtribe.TaskMaster.entity.TeamInvite;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface TeamInviteRepository extends JpaRepository<TeamInvite, UUID> {
    Optional<TeamInvite> findByToken(String token);
}
