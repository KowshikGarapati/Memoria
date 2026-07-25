package com.memoria.Memoria.services;

import com.memoria.Memoria.dto.search.SearchRequest;
import com.memoria.Memoria.dto.search.SearchResultItem;
import com.memoria.Memoria.models.User;
import org.springframework.data.domain.Page;

/**
 * Service interface for unified hybrid search across notes.
 */
public interface SearchService {
    
    /**
     * Executes a weighted hybrid search combining full-text search and vector similarity.
     *
     * @param request Search criteria including query string, filters, pagination, and sorting.
     * @param user    Authenticated user owning the notes.
     * @return Paginated search result items with scores and highlights.
     */
    Page<SearchResultItem> search(SearchRequest request, User user);
}
