package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.entity.Task;
import com.airtribe.TaskMaster.entity.TaskAssignment;
import com.airtribe.TaskMaster.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskAssignmentRepository extends JpaRepository<TaskAssignment, String> {
    Optional<TaskAssignment> findFirstByTaskAndStatusOrderByAssignedAtDesc(Task task, Status status);
    
    /**
     * Find current assignment for a task
     * @param task The task to check
     * @return Optional containing the current assignment if it exists
     */
    /**
     * Find current active (PENDING) assignment for a task
     * @param task The task to check
     * @return Optional containing the current active assignment if it exists
     */
    @Query("SELECT ta FROM TaskAssignment ta WHERE ta.task = :task AND ta.status = 'PENDING'")
    Optional<TaskAssignment> findActiveAssignment(@Param("task") Task task);

    /**
     * Delete all assignments for a task
     */
    @Modifying
    @Query("DELETE FROM TaskAssignment ta WHERE ta.task = :task")
    void deleteByTask(@Param("task") Task task);
}
