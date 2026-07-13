package com.memoria.Memoria.services;

import com.memoria.Memoria.models.Note;
import java.util.List;

public interface VectorService {
    void indexNote(Note note);
    List<Long> searchSimilar(String query, int limit);
}
