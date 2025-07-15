package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.entity.TaskArchive;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskArchiveRepository extends JpaRepository<TaskArchive, String> {
    boolean existsByTask(Task task);

    /**
     * Delete all archive records for a task
     */
    @Modifying
    @Query("DELETE FROM TaskArchive ta WHERE ta.task = :task")
    void deleteByTask(@Param("task") Task task);
}
