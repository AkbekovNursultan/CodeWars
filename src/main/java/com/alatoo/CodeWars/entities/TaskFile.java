package com.alatoo.CodeWars.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "task_files_table")
public class TaskFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Column(name = "path")
    private String path;

    @ManyToOne
    private Task task;
}
