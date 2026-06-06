package com.memoria.Memoria.repositories;

import com.memoria.Memoria.models.User;
import org.springframework.data.domain.Example;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    User findById(int id);

    User findByUsername(String Username);

    List<User> findByUsernameContainingIgnoreCase(String username);
}
