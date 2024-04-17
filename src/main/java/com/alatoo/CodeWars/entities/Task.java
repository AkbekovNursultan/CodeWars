package com.alatoo.CodeWars.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasks_table")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    private String answer;
    private Boolean approved;
    @ManyToOne
    private Difficulty difficulty;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<User> answered_users;
    @ManyToOne
    private User added_user;
    @OneToMany(cascade = CascadeType.ALL)
    private List<TaskFile> taskFiles;
}
