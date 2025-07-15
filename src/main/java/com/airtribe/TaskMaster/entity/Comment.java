package com.airtribe.TaskMaster.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {
    @Id
    @GeneratedValue
    private UUID id;

    private String text;
    private Instant createdAt;

    @ManyToOne(optional = false)
    private User author;

    @ManyToOne(optional = false)
    private Task task;
}
