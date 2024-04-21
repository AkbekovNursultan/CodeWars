package com.alatoo.CodeWars.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
public class Hint {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String hint;
    @ManyToOne
    private Task task;
    @ManyToMany
    private List<User> receivedUsers;
    ///-----
}
