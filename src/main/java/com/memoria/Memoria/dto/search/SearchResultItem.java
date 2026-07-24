package com.memoria.Memoria.dto.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Future-proof DTO representing a ranked search result item.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchResultItem {
    private Long id;
    private String title;
    private String content;
    private String summary;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Set<String> tags;
    private double score;
    private String highlight;
}
