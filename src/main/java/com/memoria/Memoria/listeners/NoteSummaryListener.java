package com.memoria.Memoria.listeners;

import com.memoria.Memoria.events.NoteSavedEvent;
import com.memoria.Memoria.services.NoteSummaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class NoteSummaryListener {

    private final NoteSummaryService noteSummaryService;

    @Async("aiTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleNoteSavedEvent(NoteSavedEvent event) {
        log.info("Received NoteSavedEvent for note ID: {}. Triggering async summary generation.", event.getNoteId());
        noteSummaryService.generateAndSaveSummary(event.getNoteId());
    }
}
