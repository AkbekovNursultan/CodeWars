package com.alatoo.CodeWars.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "difficulties")
public class DifficultyKyu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Integer pointsForTask;
    private Integer requiredPoints;
    @OneToMany
    private List<Task> tasks;
}
