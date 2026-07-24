package com.memoria.Memoria.repositories.projections;

import java.time.LocalDateTime;

/**
 * Spring Data JPA interface projection for type-safe native hybrid search query mapping.
 */
public interface SearchResultProjection {
    Long getId();
    String getTitle();
    String getContent();
    String getSummary();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();
    Double getFinalScore();
    String getHighlight();
    String getTagNames();
    Long getTotalCount();
}
