package com.memoria.Memoria.controllers;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.NoteResponse;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.services.NoteService;
import com.memoria.Memoria.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class ApiNoteController {

    private final NoteService noteService;
    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<NoteResponse>> getAllNotes(Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        List<NoteResponse> notes = noteService.findAllByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notes);
    }

    @PostMapping
    public ResponseEntity<NoteResponse> createNote(
            @Valid @RequestBody CreateNoteRequest request,
            Principal principal
    ) {
        User user = userService.requireByUsername(principal.getName());
        Note note = noteService.createNote(request, user);
        return ResponseEntity.ok(toResponse(note));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NoteResponse> getNote(@PathVariable Long id, Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        Note note = noteService.findOwnedNote(id, user);
        return ResponseEntity.ok(toResponse(note));
    }

    @PutMapping("/{id}")
    public ResponseEntity<NoteResponse> updateNote(
            @PathVariable Long id,
            @Valid @RequestBody UpdateNoteRequest request,
            Principal principal
    ) {
        User user = userService.requireByUsername(principal.getName());
        Note note = noteService.updateNote(id, request, user);
        return ResponseEntity.ok(toResponse(note));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteNote(@PathVariable Long id, Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        noteService.deleteNote(id, user);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<NoteResponse>> searchNotes(
            @RequestParam String query,
            Principal principal
    ) {
        User user = userService.requireByUsername(principal.getName());
        List<NoteResponse> notes = noteService.search(user, query).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(notes);
    }

    private NoteResponse toResponse(Note note) {
        NoteResponse response = new NoteResponse();
        response.setId(note.getId());
        response.setTitle(note.getTitle());
        response.setContent(note.getContent());
        response.setCreatedAt(note.getCreatedAt());
        response.setUpdatedAt(note.getUpdatedAt());
        response.setTags(note.getTags().stream().map(t -> t.getName()).collect(Collectors.toSet()));
        response.setSummary(note.getSummary());
        response.setSummaryStatus(note.getSummaryStatus() != null ? note.getSummaryStatus().name() : null);
        response.setSummaryGeneratedAt(note.getSummaryGeneratedAt());
        return response;
    }
}
