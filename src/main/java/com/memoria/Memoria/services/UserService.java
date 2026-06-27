package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.user.RegisterRequest;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public interface UserService {

    void register(RegisterRequest request);

    Optional<User> findById(Long id);

    Optional<User> findByEmail(String email);

    User findByUsername(String username);
}