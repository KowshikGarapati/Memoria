package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    public Note createNote(CreateNoteRequest request, User user) {

        Note note = new Note();

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        note.setUser(user);

        return noteRepository.save(note);
    }

    @Override
    public void updateNote(Long id, UpdateNoteRequest request) {

        Note note = findById(id);

        note.setTitle(request.getTitle());
        note.setContent(request.getContent());

        note.setUpdatedAt(LocalDateTime.now());

        noteRepository.save(note);
    }

    @Override
    public List<Note> getUserNotes(Long userId) {
        return noteRepository.findByUserId(userId);
    }

    @Override
    public List<Note> getNotesByUser(User user) {
        return noteRepository.findNotesByUser(user);
    }

    @Override
    public Note findById(Long id) {

        return noteRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException("Note not found."));
    }

    @Override
    public List<Note> findAllByUser(User user) {
        return noteRepository.findNotesByUser(user);
    }

    @Override
    public void deleteNote(Long noteId) {
        noteRepository.deleteById(noteId);
    }

    @Override
    public List<Note> search(String query) {

        return noteRepository
                .findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
                        query,
                        query
                );
    }
}