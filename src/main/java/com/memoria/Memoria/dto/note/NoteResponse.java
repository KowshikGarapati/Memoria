package com.memoria.Memoria.dto.note;

import java.time.LocalDateTime;

import lombok.Data;
import java.util.Set;

@Data
public class NoteResponse {
    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> tags;
    private String summary;
    private String summaryStatus;
    private LocalDateTime summaryGeneratedAt;
}
