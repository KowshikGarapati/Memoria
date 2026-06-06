package com.memoria.Memoria.services;

import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public List<User> findAllUsers(){
        return userRepository.findAll();
    }

    public User findUserById(int id){
        return userRepository.findById(Math.toIntExact(Long.valueOf(id)));
    }

    public List<User> findUsersByUsername(String username){
        return userRepository.findByUsernameContainingIgnoreCase(username);
    }

    public void createNewUser(String username, String password, String email){
        User newUser = new User(username, password, email);
        userRepository.save(newUser);
    }

    public void deleteUser(User user){
        userRepository.delete((user));
    }


}
