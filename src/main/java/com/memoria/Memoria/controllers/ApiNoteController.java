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

import com.memoria.Memoria.dto.search.SearchRequest;
import com.memoria.Memoria.dto.search.SearchResultItem;
import com.memoria.Memoria.dto.search.SearchSort;
import com.memoria.Memoria.services.SearchService;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/notes")
@RequiredArgsConstructor
public class ApiNoteController {

    private final NoteService noteService;
    private final UserService userService;
    private final SearchService searchService;

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
    public ResponseEntity<Page<SearchResultItem>> searchNotes(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) Set<String> tags,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "RELEVANCE") SearchSort sortBy,
            Principal principal
    ) {
        User user = userService.requireByUsername(principal.getName());
        SearchRequest request = new SearchRequest(query, tags, fromDate, toDate, page, size, sortBy);
        Page<SearchResultItem> results = searchService.search(request, user);
        return ResponseEntity.ok(results);
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
