package com.memoria.Memoria.services;

import com.memoria.Memoria.config.SearchProperties;
import com.memoria.Memoria.dto.search.SearchRequest;
import com.memoria.Memoria.dto.search.SearchResultItem;
import com.memoria.Memoria.dto.search.SearchSort;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.NoteRepository;
import com.memoria.Memoria.repositories.projections.SearchResultProjection;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Service implementation orchestrating weighted hybrid search and passive fallback mechanisms.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoteSearchServiceImpl implements SearchService {

    private final NoteRepository noteRepository;
    private final EmbeddingService embeddingService;
    private final SearchProperties searchProperties;

    @Override
    @Transactional(readOnly = true)
    public Page<SearchResultItem> search(SearchRequest request, User user) {
        Objects.requireNonNull(request, "SearchRequest must not be null");
        Objects.requireNonNull(user, "User must not be null");

        String rawQuery = request.getQuery();
        String normalizedQuery = (rawQuery != null && !rawQuery.isBlank()) ? rawQuery.trim() : null;

        // 1. Generate query vector synchronously (returns null if offline or empty query)
        float[] queryVector = (normalizedQuery != null) 
                ? embeddingService.getEmbeddingSync(normalizedQuery) 
                : null;

        // 2. Read configured weights directly without conditional mutation
        SearchProperties.Weights weights = searchProperties.getWeights();

        // 3. Prepare pagination & sorting
        int page = Math.max(0, request.getPage());
        int size = request.getSize() > 0 ? request.getSize() : 20;
        Pageable pageable = PageRequest.of(page, size);

        SearchSort sortMode = request.getSortBy() != null ? request.getSortBy() : SearchSort.RELEVANCE;
        Set<String> tagFilter = (request.getTags() != null && !request.getTags().isEmpty()) ? request.getTags() : null;

        log.info("Executing hybrid search for user [{}] with query ['{}'], tags {}, vectorPresent={}",
                user.getUsername(), normalizedQuery, tagFilter, (queryVector != null));

        // 4. Query repository
        List<SearchResultProjection> projections = noteRepository.findWeightedHybridSearchResults(
                user.getId(),
                normalizedQuery,
                queryVector,
                request.getFromDate(),
                request.getToDate(),
                tagFilter,
                weights.getTitle(),
                weights.getSummary(),
                weights.getTags(),
                weights.getContent(),
                weights.getKeywordBlend(),
                weights.getVectorBlend(),
                sortMode.name(),
                pageable
        );

        if (projections == null || projections.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        long totalElements = projections.get(0).getTotalCount() != null 
                ? projections.get(0).getTotalCount() 
                : projections.size();

        // 5. Map projections to SearchResultItem DTOs
        List<SearchResultItem> items = projections.stream()
                .map(this::mapProjectionToItem)
                .collect(Collectors.toList());

        return new PageImpl<>(items, pageable, totalElements);
    }

    private SearchResultItem mapProjectionToItem(SearchResultProjection p) {
        Set<String> tagSet = Collections.emptySet();
        if (p.getTagNames() != null && !p.getTagNames().isBlank()) {
            tagSet = Arrays.stream(p.getTagNames().split(","))
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        }

        return SearchResultItem.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                .summary(p.getSummary())
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .tags(tagSet)
                .score(p.getFinalScore() != null ? p.getFinalScore() : 0.0)
                .highlight(p.getHighlight())
                .build();
    }
}
