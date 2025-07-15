package com.airtribe.TaskMaster.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.*;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue
    private UUID id;

    private String filename;

    @Lob
    private byte[] data;

    @ManyToOne(optional = false)
    private Task task;
}