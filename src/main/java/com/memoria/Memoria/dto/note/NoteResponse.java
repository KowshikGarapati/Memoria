package com.memoria.Memoria.dto.note;

import java.time.LocalDateTime;

public record NoteResponse(
        Long id,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
