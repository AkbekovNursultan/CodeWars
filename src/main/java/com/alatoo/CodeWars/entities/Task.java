package com.alatoo.CodeWars.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "tasks_table")
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull private String name;
    @NotNull private String description;
    @NotNull private String answer;
    private Boolean approved;
    private Double rating;
    private LocalDateTime createdDate;

    @OneToMany(cascade = CascadeType.ALL)
    private List<Hint> hints;

    @ManyToOne
    private Difficulty difficulty;
    @ManyToMany(cascade = CascadeType.REMOVE)
    private List<Tag> tags;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<User> answeredUsers;
    @ManyToOne
    private User addedUser;
    @OneToMany(cascade = CascadeType.ALL)
    private List<TaskFile> taskFiles;
    @OneToMany
    private List<Review> reviews;
}
