package com.memoria.Memoria.dto.user;

import com.memoria.Memoria.models.Note;

import java.util.List;

public record UserResponse(
        Long id,
        String username,
        String email,
        List<Note> userNotes
) {
}
