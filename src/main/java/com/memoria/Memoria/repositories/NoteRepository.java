package com.memoria.Memoria.repositories;

import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    List<Note> findByUserId(Long userId);

    List<Note> findByTitleContainingIgnoreCase(String title);

    List<Note> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
            String title,
            String content
    );
}
