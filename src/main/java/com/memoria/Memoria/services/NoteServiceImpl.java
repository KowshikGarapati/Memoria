package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.exception.NoteNotFoundException;
import com.memoria.Memoria.exception.UnauthorizedAccessException;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final TagService tagService;

    @Override
    @Transactional
    public Note createNote(CreateNoteRequest request, User user) {
        Note note = new Note();
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());
        note.setUser(user);
        note.setTags(tagService.resolveTagsFromInput(request.getTags()));
        return noteRepository.save(note);
    }

    @Override
    @Transactional
    public Note updateNote(Long id, UpdateNoteRequest request, User user) {
        Note note = findOwnedNote(id, user);
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());
        note.setTags(tagService.resolveTagsFromInput(request.getTags()));
        return noteRepository.save(note);
    }

    @Override
    @Transactional(readOnly = true)
    public Note findById(Long id) {
        return noteRepository.findByIdWithTags(id)
                .orElseThrow(() -> new NoteNotFoundException(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Note findOwnedNote(Long id, User user) {
        Note note = findById(id);
        if (!note.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedAccessException();
        }
        return note;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> findAllByUser(User user) {
        return noteRepository.findByUserOrderByUpdatedAtDesc(user);
    }

    @Override
    @Transactional
    public void deleteNote(Long id, User user) {
        Note note = findOwnedNote(id, user);
        noteRepository.delete(note);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Note> search(User user, String query) {
        if (query == null || query.isBlank()) {
            return findAllByUser(user);
        }
        return noteRepository.searchByUserAndQuery(user, query.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public long countByUser(User user) {
        return noteRepository.countByUser(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UpdateNoteRequest toUpdateRequest(Note note) {
        UpdateNoteRequest request = new UpdateNoteRequest();
        request.setTitle(note.getTitle());
        request.setContent(note.getContent());
        request.setTags(tagService.formatTagsForInput(note.getTags()));
        return request;
    }
}
