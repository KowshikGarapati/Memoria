package com.memoria.Memoria.models;

import jakarta.persistence.* ;

import java.util.List ;

import lombok.* ;

@Entity
@Table(name="users")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    int id;

    String username;
    String password;
    String email;

    @OneToMany(mappedBy = "user", cascade= CascadeType.ALL)
    List<Note> notes;

    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
