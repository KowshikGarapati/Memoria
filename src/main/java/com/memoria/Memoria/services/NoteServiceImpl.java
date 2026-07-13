package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.SearchResultDTO;
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
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class NoteServiceImpl implements NoteService {

    private final NoteRepository noteRepository;
    private final TagService tagService;
    private final EmbeddingService embeddingService;
    private final AiService aiService; // Anthropic for summaries

    @Override
    @Transactional
    public Note createNote(CreateNoteRequest request, User user) {
        Note note = new Note();
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());
        note.setUser(user);
        note.setTags(tagService.resolveTagsFromInput(request.getTags()));
        
        // Generate embedding synchronously for simplicity in Phase 2
        String textToEmbed = note.getTitle() + " " + note.getContent();
        double[] embedding = embeddingService.getEmbedding(textToEmbed).block();
        note.setEmbedding(embedding);
        
        return noteRepository.save(note);
    }

    @Override
    @Transactional
    public Note updateNote(Long id, UpdateNoteRequest request, User user) {
        Note note = findOwnedNote(id, user);
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());
        note.setTags(tagService.resolveTagsFromInput(request.getTags()));
        
        String textToEmbed = note.getTitle() + " " + note.getContent();
        double[] embedding = embeddingService.getEmbedding(textToEmbed).block();
        note.setEmbedding(embedding);
        
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
    public List<SearchResultDTO> searchHybrid(User user, String query) {
        if (query == null || query.isBlank()) {
            return List.of();
        }

        double[] queryVector = embeddingService.getEmbedding(query).block();
        List<Object[]> results = noteRepository.findHybridSearchResults(user.getId(), query, queryVector);

        System.out.println("Hybrid results:");
        System.out.println(results.size());

        return results.stream().map(row -> {
            for (int i = 0; i < row.length; i++) {
                System.out.println(
                    i + " -> " +
                    (row[i] == null ? "null" : row[i].getClass().getName())
                );
            }
            SearchResultDTO dto = new SearchResultDTO();
            dto.setId(((Number) row[0]).longValue());
            dto.setTitle((String) row[1]);
            dto.setContent((String) row[2]);
            dto.setCreatedAt((LocalDateTime) row[3]);
            dto.setUpdatedAt((LocalDateTime) row[4]);
            
            // Fetch tags for this note
            Note note = noteRepository.findByIdWithTags(dto.getId()).orElse(null);
            if (note != null) {
                dto.setTags(note.getTags().stream().map(t -> t.getName()).collect(java.util.stream.Collectors.toSet()));
            }
            
            dto.setScore(((Number) row[6]).doubleValue());
            dto.setHighlight((String) row[7]);
            return dto;
        }).collect(java.util.stream.Collectors.toList());
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
