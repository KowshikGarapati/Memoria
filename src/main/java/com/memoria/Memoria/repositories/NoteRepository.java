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
}
