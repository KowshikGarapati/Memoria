package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;

import java.util.List;

public interface NoteService {

    Note createNote(CreateNoteRequest request, User user);

    Note updateNote(Long id, UpdateNoteRequest request, User user);

    Note findById(Long id);

    Note findOwnedNote(Long id, User user);

    List<Note> findAllByUser(User user);

    void deleteNote(Long id, User user);

    List<Note> search(User user, String query);

    long countByUser(User user);

    UpdateNoteRequest toUpdateRequest(Note note);
}
