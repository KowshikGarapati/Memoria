package com.memoria.Memoria.models;

import jakarta.persistence.* ;

import lombok.* ;

import java.time.LocalDateTime;
import java.util.List ;

public class Note {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String title ;
    private String content ;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    private LocalDateTime createdAt ;

}
