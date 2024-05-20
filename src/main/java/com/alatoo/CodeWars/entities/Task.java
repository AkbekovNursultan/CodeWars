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
    private Double rating = 0.0;
    private LocalDateTime createdDate;

    @OneToMany(mappedBy = "task",cascade = CascadeType.REMOVE)
    private List<Hint> hints;

    @ManyToOne
    private DifficultyKyu difficulty;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Tag> tags;

    @ManyToMany(cascade = CascadeType.ALL)
    private List<User> answeredUsers;

    private Integer solved = 0;
    @ManyToOne
    private User addedUser;
    @OneToMany(cascade = CascadeType.ALL)
    private List<TaskFile> taskFiles;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Review> reviews;
    @ManyToMany
    private List<User> markedUsers;

}
