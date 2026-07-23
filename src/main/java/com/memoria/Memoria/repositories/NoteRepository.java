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
            WITH semantic_search AS (
                SELECT id, (1 - (embedding <=> cast(:queryVector as vector))) as semantic_score
                FROM note
                WHERE user_id = :userId
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
