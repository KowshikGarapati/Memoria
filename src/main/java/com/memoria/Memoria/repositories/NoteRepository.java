package com.memoria.Memoria.repositories;

import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NoteRepository extends JpaRepository<Note, Long> {

    @EntityGraph(attributePaths = "tags")
    List<Note> findByUserOrderByUpdatedAtDesc(User user);

    long countByUser(User user);

    @EntityGraph(attributePaths = "tags")
    @Query("SELECT n FROM Note n WHERE n.id = :id")
    Optional<Note> findByIdWithTags(@Param("id") Long id);

    @EntityGraph(attributePaths = "tags")
    @Query("""
            SELECT DISTINCT n FROM Note n
            LEFT JOIN n.tags t
            WHERE n.user = :user
            AND (
                LOWER(n.title) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(n.content) LIKE LOWER(CONCAT('%', :query, '%'))
                OR LOWER(t.name) LIKE LOWER(CONCAT('%', :query, '%'))
            )
            ORDER BY n.updatedAt DESC
            """)
    List<Note> searchByUserAndQuery(@Param("user") User user, @Param("query") String query);

    @Query(value = """
            WITH note_documents AS (
                SELECT 
                    n.id,
                    n.title,
                    n.content,
                    n.summary,
                    n.created_at,
                    n.updated_at,
                    n.user_id,
                    n.embedding,
                    COALESCE(string_agg(t.name, ' '), '') AS combined_tags,
                    COALESCE(string_agg(t.name, ','), '') AS tag_names,
                    (
                        setweight(to_tsvector('english', COALESCE(n.title, '')), 'A') ||
                        setweight(to_tsvector('english', COALESCE(n.summary, '')), 'B') ||
                        setweight(to_tsvector('english', COALESCE(string_agg(t.name, ' '), '')), 'C') ||
                        setweight(to_tsvector('english', COALESCE(n.content, '')), 'D')
                    ) AS full_document
                FROM note n
                LEFT JOIN note_tags nt ON n.id = nt.note_id
                LEFT JOIN tags t ON nt.tag_id = t.id
                WHERE n.user_id = :userId
                  AND (cast(:fromDate as timestamp) IS NULL OR n.created_at >= cast(:fromDate as timestamp))
                  AND (cast(:toDate as timestamp) IS NULL OR n.created_at <= cast(:toDate as timestamp))
                GROUP BY n.id
            ),
            scored_notes AS (
                SELECT 
                    nd.id,
                    nd.title,
                    nd.content,
                    nd.summary,
                    nd.created_at AS createdAt,
                    nd.updated_at AS updatedAt,
                    nd.tag_names AS tagNames,
                    ts_headline('english', 
                                COALESCE(nd.title, '') || ' ' || COALESCE(nd.summary, '') || ' ' || COALESCE(nd.content, ''), 
                                plainto_tsquery('english', COALESCE(cast(:query as text), '')), 
                                'StartSel=<b>, StopSel=</b>, MaxWords=35, MinWords=15') AS highlight,
                    (
                        COALESCE(ts_rank_cd(
                            ARRAY[:contentWeight, :tagsWeight, :summaryWeight, :titleWeight]::float4[], 
                            nd.full_document, 
                            plainto_tsquery('english', COALESCE(cast(:query as text), ''))
                        ), 0) * :keywordBlend
                        +
                        CASE 
                            WHEN :queryVector IS NOT NULL AND nd.embedding IS NOT NULL 
                            THEN (1 - (nd.embedding <=> cast(:queryVector as vector))) * :vectorBlend
                            ELSE 0
                        END
                    ) AS finalScore
                FROM note_documents nd
                WHERE (
                    (
                        -- Browsing mode: no query text and no vector
                        (cast(:query as text) IS NULL OR cast(:query as text) = '') AND :queryVector IS NULL
                    ) OR (
                        -- Search mode: text match OR semantic match above threshold
                        (cast(:query as text) IS NOT NULL AND cast(:query as text) <> '' AND numnode(plainto_tsquery('english', cast(:query as text))) > 0 AND nd.full_document @@ plainto_tsquery('english', cast(:query as text)))
                        OR
                        (:queryVector IS NOT NULL AND nd.embedding IS NOT NULL AND (1 - (nd.embedding <=> cast(:queryVector as vector))) >= :minVectorSimilarity)
                    )
                )
                  AND (cast(:tagNamesFilter as text[]) IS NULL OR EXISTS (
                          SELECT 1 FROM note_tags nt2 JOIN tags t2 ON nt2.tag_id = t2.id 
                          WHERE nt2.note_id = nd.id AND t2.name IN (:tagNamesFilter)
                      ))
            )
            SELECT sn.id AS id,
                   sn.title AS title,
                   sn.content AS content,
                   sn.summary AS summary,
                   sn.createdAt AS createdAt,
                   sn.updatedAt AS updatedAt,
                   sn.tagNames AS tagNames,
                   sn.highlight AS highlight,
                   sn.finalScore AS finalScore,
                   COUNT(*) OVER() AS totalCount
            FROM scored_notes sn
            ORDER BY 
                CASE WHEN cast(:sortBy as text) = 'NEWEST' THEN sn.createdAt END DESC,
                CASE WHEN cast(:sortBy as text) = 'OLDEST' THEN sn.createdAt END ASC,
                CASE WHEN cast(:sortBy as text) = 'RELEVANCE' OR cast(:sortBy as text) IS NULL THEN sn.finalScore END DESC,
                sn.updatedAt DESC
            """, nativeQuery = true)
    List<com.memoria.Memoria.repositories.projections.SearchResultProjection> findWeightedHybridSearchResults(
            @Param("userId") Long userId,
            @Param("query") String query,
            @Param("queryVector") float[] queryVector,
            @Param("fromDate") java.time.LocalDateTime fromDate,
            @Param("toDate") java.time.LocalDateTime toDate,
            @Param("tagNamesFilter") java.util.Set<String> tagNamesFilter,
            @Param("minVectorSimilarity") double minVectorSimilarity,
            @Param("titleWeight") double titleWeight,
            @Param("summaryWeight") double summaryWeight,
            @Param("tagsWeight") double tagsWeight,
            @Param("contentWeight") double contentWeight,
            @Param("keywordBlend") double keywordBlend,
            @Param("vectorBlend") double vectorBlend,
            @Param("sortBy") String sortBy,
            org.springframework.data.domain.Pageable pageable
    );

    @Query(value = """
            WITH semantic_search AS (
                SELECT id, 
                       CASE 
                           WHEN embedding IS NOT NULL AND :queryVector IS NOT NULL 
                           THEN (1 - (embedding <=> cast(:queryVector as vector))) 
                           ELSE 0 
                       END as semantic_score
                FROM note
                WHERE user_id = :userId AND embedding IS NOT NULL
            ),
            keyword_search AS (
                SELECT id, ts_rank_cd(to_tsvector('english', title || ' ' || content), plainto_tsquery('english', :query)) as keyword_score,
                       ts_headline('english', content, plainto_tsquery('english', :query), 'StartSel=<b>, StopSel=</b>, MaxWords=35, MinWords=15') as highlight
                FROM note
                WHERE user_id = :userId
                AND to_tsvector('english', title || ' ' || content) @@ plainto_tsquery('english', :query)
            )
            SELECT n.id, n.title, n.content, n.created_at, n.updated_at, n.user_id,
                   (COALESCE(k.keyword_score, 0) * 0.4 + COALESCE(s.semantic_score, 0) * 0.6) as final_score,
                   k.highlight
            FROM note n
            LEFT JOIN semantic_search s ON n.id = s.id
            LEFT JOIN keyword_search k ON n.id = k.id
            WHERE n.user_id = :userId
            AND (k.id IS NOT NULL OR (s.semantic_score IS NOT NULL AND s.semantic_score > 0.7))
            ORDER BY final_score DESC
            LIMIT 20
            """, nativeQuery = true)
    List<Object[]> findHybridSearchResults(@Param("userId") Long userId, 
                                          @Param("query") String query, 
                                          @Param("queryVector") float[] queryVector);
}
