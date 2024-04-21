package com.alatoo.CodeWars.entities;

import com.alatoo.CodeWars.enums.Role;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "users_table")
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String password;
    private String email;
    private Boolean emailVerified;
    private Role role;

    private String verificationCode;
    private String recoveryCode;
    private Integer points;
    private Boolean banned;
    private Integer rank;

    @OneToOne(cascade = CascadeType.ALL)
    private Image image;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Task> solvedTasks;
    @OneToMany(cascade = CascadeType.ALL)
    private List<Task> createdTasks;
    @ManyToMany(cascade = CascadeType.ALL)
    private List<Hint> usedHints;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
