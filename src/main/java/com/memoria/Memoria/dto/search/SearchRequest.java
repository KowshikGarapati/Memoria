package com.memoria.Memoria.dto.search;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Encapsulates search query criteria, tag/date filters, and pagination parameters.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    private String query;
    private Set<String> tags;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
    private int page = 0;
    private int size = 20;
    private SearchSort sortBy = SearchSort.RELEVANCE;
}
