package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.SearchResultDTO;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.exception.NoteNotFoundException;
import com.memoria.Memoria.exception.UnauthorizedAccessException;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.NoteRepository;
import com.memoria.Memoria.events.NoteSavedEvent;
import com.memoria.Memoria.models.SummaryStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
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
    private final AiService aiService; // Local Ollama AI service for summaries & chat
    private final ApplicationEventPublisher eventPublisher;

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
        try {
            float[] embedding = embeddingService.getEmbedding(textToEmbed).block();
            note.setEmbedding(embedding);
        } catch (Exception e) {
            note.setEmbedding(null);
        }
        
        note.setSummaryStatus(SummaryStatus.PENDING);
        Note savedNote = noteRepository.save(note);
        eventPublisher.publishEvent(new NoteSavedEvent(savedNote.getId()));
        return savedNote;
    }

    @Override
    @Transactional
    public Note updateNote(Long id, UpdateNoteRequest request, User user) {
        Note note = findOwnedNote(id, user);
        note.setTitle(request.getTitle().trim());
        note.setContent(request.getContent().trim());
        note.setTags(tagService.resolveTagsFromInput(request.getTags()));
        
        String textToEmbed = note.getTitle() + " " + note.getContent();
        try {
            float[] embedding = embeddingService.getEmbedding(textToEmbed).block();
            note.setEmbedding(embedding);
        } catch (Exception e) {
            note.setEmbedding(null);
        }
        
        // Preserve previous summary but mark status as PENDING for regeneration
        note.setSummaryStatus(SummaryStatus.PENDING);
        
        Note savedNote = noteRepository.save(note);
        eventPublisher.publishEvent(new NoteSavedEvent(savedNote.getId()));
        return savedNote;
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

        float[] queryVector = embeddingService.getEmbedding(query).block();
        List<Object[]> results = noteRepository.findHybridSearchResults(user.getId(), query, queryVector);

        return results.stream().map(row -> {
            SearchResultDTO dto = new SearchResultDTO();
            dto.setId(((Number) row[0]).longValue());
            dto.setTitle((String) row[1]);
            dto.setContent((String) row[2]);

            if (row[3] instanceof java.sql.Timestamp ts) {
                dto.setCreatedAt(ts.toLocalDateTime());
            } else if (row[3] instanceof LocalDateTime ldt) {
                dto.setCreatedAt(ldt);
            }

            if (row[4] instanceof java.sql.Timestamp ts) {
                dto.setUpdatedAt(ts.toLocalDateTime());
            } else if (row[4] instanceof LocalDateTime ldt) {
                dto.setUpdatedAt(ldt);
            }
            
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
