package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.entity.Team;
import com.airtribe.TaskMaster.entity.Comment;
import com.airtribe.TaskMaster.entity.Attachment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface TeamRepository extends JpaRepository<Team, UUID> { }
