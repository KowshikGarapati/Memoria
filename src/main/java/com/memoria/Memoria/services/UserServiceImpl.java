package com.memoria.Memoria.services;
import com.memoria.Memoria.dto.user.RegisterRequest;
import com.memoria.Memoria.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.Repository;
import org.springframework.stereotype.Service;
import com.memoria.Memoria.models.User;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private final UserRepository userRepository;

    @Override
    public void register(RegisterRequest request) {
        if (request.password().toString().equals(request.confirmedPassword().toString())) {
            String encodedPassword = passwordEncoder.encode(request.password());
            User user = new User(
                    request.username(),
                    request.email(),
                    encodedPassword
            );
            userRepository.save(user);
            System.out.println("User added:" + user.getUsername());
        }else {
            System.out.println("passwords doesn't match." + request.password() + "and" + request.confirmedPassword());
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public User findByUsername(String username){ return userRepository.findByUsername(username); }
}
