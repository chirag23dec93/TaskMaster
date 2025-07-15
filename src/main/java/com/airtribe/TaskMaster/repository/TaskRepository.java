package com.airtribe.TaskMaster.repository;

import com.airtribe.TaskMaster.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.lang.NonNull;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, String> {
    List<Task> findByTitleContainingIgnoreCaseAndDeletedFalse(String title);

    List<Task> findByDescriptionContainingIgnoreCaseAndDeletedFalse(String description);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "ASC")
    List<Task> findByTitleContainingIgnoreCaseAndDeletedFalseAsc(
            @Param("title") String title,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "DESC")
    List<Task> findByTitleContainingIgnoreCaseAndDeletedFalseDesc(
            @Param("title") String title,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "ASC")
    List<Task> findByDescriptionContainingIgnoreCaseAndDeletedFalseAsc(
            @Param("description") String description,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "DESC")
    List<Task> findByDescriptionContainingIgnoreCaseAndDeletedFalseDesc(
            @Param("description") String description,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "ASC")
    List<Task> findByTitleAndDescriptionContainingIgnoreCaseAsc(
            @Param("title") String title,
            @Param("description") String description,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "AND LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%')) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "DESC")
    List<Task> findByTitleAndDescriptionContainingIgnoreCaseDesc(
            @Param("title") String title,
            @Param("description") String description,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "ASC")
    List<Task> findByTitleOrDescriptionContainingIgnoreCaseAsc(
            @Param("title") String title,
            @Param("description") String description,
            @Param("sortField") String sortField);

    @Query(value = "SELECT t FROM Task t WHERE t.deleted = false AND " +
           "(LOWER(t.title) LIKE LOWER(CONCAT('%', :title, '%')) OR LOWER(t.description) LIKE LOWER(CONCAT('%', :description, '%'))) " +
           "ORDER BY CASE " +
           "    WHEN :sortField = 'title' THEN t.title " +
           "    WHEN :sortField = 'description' THEN t.description " +
           "    ELSE t.createdAt " +
           "END " +
           "DESC")
    List<Task> findByTitleOrDescriptionContainingIgnoreCaseDesc(
            @Param("title") String title,
            @Param("description") String description,
            @Param("sortField") String sortField);
    
    @Override
    @Query("SELECT t FROM Task t WHERE t.id = :id AND t.deleted = false")
    @NonNull
    Optional<Task> findById(@NonNull @Param("id") String id);
}
