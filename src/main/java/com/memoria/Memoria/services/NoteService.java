package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NoteService {
    @Autowired
    NoteRepository noteRepository;

    public List<Note> findNoteByTitle(String title){
        return noteRepository.findByTitleContainingIgnoreCase(title);
    }
}
