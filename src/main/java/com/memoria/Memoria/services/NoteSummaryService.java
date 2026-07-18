package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.SummaryStatus;
import com.memoria.Memoria.repositories.NoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class NoteSummaryService {

    private final NoteRepository noteRepository;
    private final AiService aiService;

    /**
     * Executes the main task of generating the summary.
     * Note content is loaded and the external AI service is invoked outside of active database transactions
     * to avoid holding open connection pool resources for too long.
     */
    public void generateAndSaveSummary(Long noteId) {
        // Step 1: Read note content outside a write transaction
        String content = noteRepository.findById(noteId)
                .map(Note::getContent)
                .orElse(null);

        if (content == null || content.isBlank()) {
            log.warn("Note {} not found or content is empty. Skipping summary generation.", noteId);
            updateSummaryStatus(noteId, null, SummaryStatus.FAILED);
            return;
        }

        // Step 2: Invoke slow Claude API outside transaction
        try {
            log.info("Generating summary via Anthropic Claude for note ID: {}", noteId);
            String summary = aiService.summarize(content).block();
            updateSummaryStatus(noteId, summary, SummaryStatus.SUCCESS);
        } catch (Exception e) {
            log.error("Error occurred while calling AI summary API for note ID: " + noteId, e);
            updateSummaryStatus(noteId, null, SummaryStatus.FAILED);
        }
    }

    /**
     * Updates the summary content and status in the database using a new transaction.
     * Keeps database connections open only for a brief write operation.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateSummaryStatus(Long noteId, String summary, SummaryStatus status) {
        noteRepository.findById(noteId).ifPresentOrElse(note -> {
            if (status == SummaryStatus.SUCCESS) {
                note.setSummary(summary);
                note.setSummaryGeneratedAt(LocalDateTime.now());
            }
            // If status is FAILED, note.getSummary() is left unchanged (preserving previous summary)
            note.setSummaryStatus(status);
            noteRepository.save(note);
            log.info("Updated database status to {} for note ID: {}", status, noteId);
        }, () -> log.error("Unable to update summary status. Note ID: {} not found.", noteId));
    }
}
