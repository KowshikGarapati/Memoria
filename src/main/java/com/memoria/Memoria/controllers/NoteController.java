package com.memoria.Memoria.controllers;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.services.NoteService;
import com.memoria.Memoria.services.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Optional;

@Controller
@RequestMapping("/notes")
@RequiredArgsConstructor
public class NoteController {

    private final NoteService noteService;
    private final UserService userService;

    @GetMapping
    public String notesHome(Model model, Principal principal){

        User user = userService.findByUsername(principal.getName());

        model.addAttribute("loggedUser", user);
        model.addAttribute("notes", noteService.findAllByUser(user));

        return "home";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model){

        model.addAttribute("noteRequest", new CreateNoteRequest());

        return "new_note";
    }

    @PostMapping
    public String createNote(@ModelAttribute CreateNoteRequest request,
                             Principal principal){

        User user = userService.findByUsername(principal.getName());

        noteService.createNote(request, user);

        return "redirect:/notes";
    }

    @GetMapping("/{id}")
    public String viewNote(@PathVariable Long id,
                           Model model){
        Note note = noteService.findById(id);
        model.addAttribute("note", note);

        return "view_note";
    }

    @GetMapping("/{id}/edit")
    public String editNote(@PathVariable Long id,
                           Model model){

        Note note = noteService.findById(id);

        model.addAttribute("noteRequest", note);

        return "edit_note";
    }

    @PostMapping("/{id}/edit")
    public String updateNote(@PathVariable Long id,
                             @ModelAttribute UpdateNoteRequest request){

        noteService.updateNote(id, request);

        return "redirect:/notes/" + id;
    }

    @PostMapping("/{id}/delete")
    public String deleteNote(@PathVariable Long id){

        noteService.deleteNote(id);

        return "redirect:/notes";
    }

}