package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface NoteService {

    Note createNote(Note note);

    List<Note> getUserNotes(Long userId);

    Optional<Note> getNote(Long noteId);

    void deleteNote(Long noteId);

    List<Note> search(String query);
}
