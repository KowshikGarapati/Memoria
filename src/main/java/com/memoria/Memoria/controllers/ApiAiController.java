package com.memoria.Memoria.controllers;

import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.services.AiService;
import com.memoria.Memoria.services.NoteService;
import com.memoria.Memoria.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class ApiAiController {

    private final AiService aiService;
    private final NoteService noteService;
    private final UserService userService;

    @PostMapping("/summarize")
    public Mono<ResponseEntity<String>> summarize(@RequestBody String content) {
        return aiService.summarize(content)
                .map(ResponseEntity::ok);
    }

    @PostMapping("/chat")
    public Mono<ResponseEntity<String>> chat(@RequestParam String query, Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        // For now, we use simple search results as context until pgvector is implemented
        List<Note> relevantNotes = noteService.search(user, query);
        String context = relevantNotes.stream()
                .map(n -> "Title: " + n.getTitle() + "\nContent: " + n.getContent())
                .collect(Collectors.joining("\n---\n"));

        return aiService.chat(query, context)
                .map(ResponseEntity::ok);
    }
}
