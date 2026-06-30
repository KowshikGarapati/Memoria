package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.user.RegisterRequest;
import com.memoria.Memoria.models.User;

import java.util.Optional;

public interface UserService {

    void register(RegisterRequest request);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User requireByUsername(String username);
}
