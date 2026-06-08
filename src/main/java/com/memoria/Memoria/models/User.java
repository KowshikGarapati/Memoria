package com.memoria.Memoria.models;

import jakarta.persistence.* ;

import java.util.ArrayList;
import java.util.List ;

import lombok.* ;

@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @OneToMany(mappedBy = "user")
    private List<Note> notes = new ArrayList<>();
}