package com.memoria.Memoria.services;
import com.memoria.Memoria.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.memoria.Memoria.models.Note;
import java.util.List;
@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;

    @Override
    public Note createNote(Note note) {

        note.setCreatedAt(LocalDateTime.now());
        note.setUpdatedAt(LocalDateTime.now());

        return noteRepository.save(note);
    }

    @Override
    public List<Note> getUserNotes(Long userId) {
        return noteRepository.findByUserId(userId);
    }

    @Override
    public Optional<Note> getNote(Long noteId) {
        return noteRepository.findById(noteId);
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
