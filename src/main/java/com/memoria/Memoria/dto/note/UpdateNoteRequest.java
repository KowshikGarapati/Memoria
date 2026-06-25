package com.memoria.Memoria.dto.note;

public record UpdateNoteRequest(
        String title,
        String content
) {
}
