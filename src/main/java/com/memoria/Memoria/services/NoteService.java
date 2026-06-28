package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;

import java.util.List;

public interface NoteService {

    Note createNote(CreateNoteRequest request, User user);

    void updateNote(Long id, UpdateNoteRequest request);

    List<Note> getUserNotes(Long userId);

    List<Note> getNotesByUser(User user);

    Note findById(Long id);

    List<Note> findAllByUser(User user);

    void deleteNote(Long noteId);

    List<Note> search(String query);

}