package com.alatoo.CodeWars.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NotNull private String text;
    @NotNull
    private Double rating;

    @ManyToOne
    private User user;

    @ManyToOne
    private Task task;
}
