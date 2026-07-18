package com.memoria.Memoria.events;

import lombok.Getter;

@Getter
public class NoteSavedEvent {
    private final Long noteId;

    public NoteSavedEvent(Long noteId) {
        this.noteId = noteId;
    }
}
