package com.memoria.Memoria.controllers;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.services.NoteService;
import com.memoria.Memoria.services.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    @GetMapping
    public String notesHome(Model model, Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        model.addAttribute("loggedUser", user);
        model.addAttribute("notes", noteService.findAllByUser(user));
        return "home";
    }

    @GetMapping("/search")
    public String searchNotes(@RequestParam(required = false) String query,
                              Model model,
                              Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        model.addAttribute("loggedUser", user);
        model.addAttribute("notes", noteService.search(user, query));
        model.addAttribute("searchQuery", query);
        return "home";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("noteRequest", new CreateNoteRequest());
        return "new_note";
    }

    @PostMapping
    public String createNote(@Valid @ModelAttribute("noteRequest") CreateNoteRequest request,
                             BindingResult bindingResult,
                             Principal principal) {
        if (bindingResult.hasErrors()) {
            return "new_note";
        }

        User user = userService.requireByUsername(principal.getName());
        Note note = noteService.createNote(request, user);
        return "redirect:/notes/" + note.getId();
    }

    @GetMapping("/{id}")
    public String viewNote(@PathVariable Long id,
                           Model model,
                           Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        Note note = noteService.findOwnedNote(id, user);
        model.addAttribute("note", note);
        return "view_note";
    }

    @GetMapping("/{id}/edit")
    public String editNote(@PathVariable Long id,
                           Model model,
                           Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        Note note = noteService.findOwnedNote(id, user);
        model.addAttribute("noteId", id);
        model.addAttribute("noteRequest", noteService.toUpdateRequest(note));
        return "edit_note";
    }

    @PostMapping("/{id}/edit")
    public String updateNote(@PathVariable Long id,
                             @Valid @ModelAttribute("noteRequest") UpdateNoteRequest request,
                             BindingResult bindingResult,
                             Model model,
                             Principal principal) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("noteId", id);
            return "edit_note";
        }

        User user = userService.requireByUsername(principal.getName());
        noteService.updateNote(id, request, user);
        return "redirect:/notes/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteNote(@PathVariable Long id,
                             Principal principal) {
        User user = userService.requireByUsername(principal.getName());
        noteService.deleteNote(id, user);
        return "redirect:/notes";
    }
}
