package com.memoria.Memoria.config;

import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        System.out.println(username);
        User user = userRepository.findByUsername(username);
        System.out.println("User found:" + user);
        if (user != null) {
            System.out.println("DB Username = " + user.getUsername());
            System.out.println("DB Password = " + user.getPassword());
        }
        return new CustomUserDetails(user);
    }
}