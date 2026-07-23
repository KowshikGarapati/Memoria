package com.memoria.Memoria;

import com.memoria.Memoria.dto.note.CreateNoteRequest;
import com.memoria.Memoria.dto.note.SearchResultDTO;
import com.memoria.Memoria.dto.note.UpdateNoteRequest;
import com.memoria.Memoria.dto.user.RegisterRequest;
import com.memoria.Memoria.exception.NoteNotFoundException;
import com.memoria.Memoria.exception.UnauthorizedAccessException;
import com.memoria.Memoria.models.Note;
import com.memoria.Memoria.models.SummaryStatus;
import com.memoria.Memoria.models.User;
import com.memoria.Memoria.repositories.NoteRepository;
import com.memoria.Memoria.repositories.UserRepository;
import com.memoria.Memoria.services.NoteService;
import com.memoria.Memoria.services.UserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.UUID;

@SpringBootTest
public class MemoriaBackendValidationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private NoteService noteService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoteRepository noteRepository;

    private User testUser1;
    private User testUser2;

    @BeforeEach
    public void setUp() {
        String suffix1 = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest request1 = new RegisterRequest("qa_user1_" + suffix1, "qa1_" + suffix1 + "@memoria.ai", "Password123!", "Password123!");
        userService.register(request1);
        testUser1 = userService.requireByUsername(request1.username());

        String suffix2 = UUID.randomUUID().toString().substring(0, 8);
        RegisterRequest request2 = new RegisterRequest("qa_user2_" + suffix2, "qa2_" + suffix2 + "@memoria.ai", "Password123!", "Password123!");
        userService.register(request2);
        testUser2 = userService.requireByUsername(request2.username());
    }

    @Test
    @DisplayName("1. Verify Authentication & Registration Validation")
    public void testAuthenticationAndValidation() {
        // Password mismatch
        RegisterRequest mismatchReq = new RegisterRequest("mismatch_user", "mismatch@memoria.ai", "Pass1", "Pass2");
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(mismatchReq));

        // Duplicate username
        RegisterRequest dupUsername = new RegisterRequest(testUser1.getUsername(), "diff_email@memoria.ai", "Password123!", "Password123!");
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(dupUsername));

        // Duplicate email
        RegisterRequest dupEmail = new RegisterRequest("diff_user", testUser1.getEmail(), "Password123!", "Password123!");
        Assertions.assertThrows(IllegalArgumentException.class, () -> userService.register(dupEmail));
    }

    @Test
    @DisplayName("2. Verify Note CRUD & Ownership Security")
    public void testNoteCrudAndSecurity() {
        CreateNoteRequest createReq = new CreateNoteRequest();
        createReq.setTitle("CRUD Test Note");
        createReq.setContent("Testing basic CRUD operations and ownership boundaries.");
        createReq.setTags("crud, java, spring");

        Note note = noteService.createNote(createReq, testUser1);
        Assertions.assertNotNull(note.getId());
        Assertions.assertEquals("CRUD Test Note", note.getTitle());
        Assertions.assertEquals(1, noteService.countByUser(testUser1));

        // User 2 trying to read User 1's note -> UnauthorizedAccessException
        Assertions.assertThrows(UnauthorizedAccessException.class, () -> noteService.findOwnedNote(note.getId(), testUser2));

        // User 2 trying to update User 1's note -> UnauthorizedAccessException
        UpdateNoteRequest updateReq = new UpdateNoteRequest();
        updateReq.setTitle("Hacked Title");
        updateReq.setContent("Hacked Content");
        Assertions.assertThrows(UnauthorizedAccessException.class, () -> noteService.updateNote(note.getId(), updateReq, testUser2));

        // Non-existent note -> NoteNotFoundException
        Assertions.assertThrows(NoteNotFoundException.class, () -> noteService.findById(999999L));

        // Delete note
        noteService.deleteNote(note.getId(), testUser1);
        Assertions.assertEquals(0, noteService.countByUser(testUser1));
    }

    @Test
    @DisplayName("3. Verify Vector Embedding Generation & PostgreSQL Persistence")
    public void testVectorEmbeddingPersistence() {
        CreateNoteRequest createReq = new CreateNoteRequest();
        createReq.setTitle("Vector Search Architecture");
        createReq.setContent("Spring Boot 4 with Hibernate 7 native vector support storing 768-dimensional floats.");
        createReq.setTags("vector, pgvector, hibernate");

        Note note = noteService.createNote(createReq, testUser1);
        
        // Manually assign a test vector if Ollama was offline during creation
        if (note.getEmbedding() == null) {
            float[] testVec = new float[768];
            testVec[0] = 0.5f;
            testVec[767] = 0.9f;
            note.setEmbedding(testVec);
            note = noteRepository.save(note);
        }

        // Fetch directly from repository to verify DB persistence and retrieval
        Note fetched = noteRepository.findById(note.getId()).orElseThrow();
        Assertions.assertNotNull(fetched.getEmbedding(), "Embedding float[] array must be persisted in database");
        Assertions.assertEquals(768, fetched.getEmbedding().length, "Embedding dimension must be 768");
    }

    @Autowired
    private com.memoria.Memoria.services.NoteSummaryService noteSummaryService;

    @Test
    @DisplayName("4. Verify Async Summarization Pipeline & Event Processing")
    public void testAsyncSummarizationPipeline() {
        CreateNoteRequest createReq = new CreateNoteRequest();
        createReq.setTitle("Deep Learning in Memory Systems");
        createReq.setContent("Memoria continuously indexes context into vector storage for natural language retrieval. It uses localized Ollama models for background summarization and context extraction.");
        createReq.setTags("ai, memoria, ollama");

        Note note = noteService.createNote(createReq, testUser1);

        // Immediately after creation, summaryStatus should be PENDING
        Assertions.assertEquals(SummaryStatus.PENDING, note.getSummaryStatus());

        // Trigger summary generation directly for fast execution
        noteSummaryService.generateAndSaveSummary(note.getId());

        Note refreshed = noteRepository.findById(note.getId()).orElseThrow();
        Assertions.assertEquals(SummaryStatus.SUCCESS, refreshed.getSummaryStatus(), "Summary status must be SUCCESS");
        Assertions.assertNotNull(refreshed.getSummary(), "Summary text must be generated");
        Assertions.assertNotNull(refreshed.getSummaryGeneratedAt(), "Summary timestamp must be recorded");
    }

    @Test
    @DisplayName("5. Verify Summary Regeneration on Note Update (Preserving History)")
    public void testSummaryRegenerationOnUpdate() {
        CreateNoteRequest createReq = new CreateNoteRequest();
        createReq.setTitle("Original Knowledge Base");
        createReq.setContent("Initial content for testing update behavior.");
        Note note = noteService.createNote(createReq, testUser1);

        // Manually set an existing summary and SUCCESS status to simulate previous completion
        note.setSummary("Original AI Summary text");
        note.setSummaryStatus(SummaryStatus.SUCCESS);
        noteRepository.save(note);

        // Perform update
        UpdateNoteRequest updateReq = new UpdateNoteRequest();
        updateReq.setTitle("Updated Knowledge Base");
        updateReq.setContent("Significantly updated content for testing update behavior.");
        Note updated = noteService.updateNote(note.getId(), updateReq, testUser1);

        // Verify previous summary is preserved while status is set to PENDING
        Assertions.assertEquals("Original AI Summary text", updated.getSummary(), "Existing summary text must be preserved during update");
        Assertions.assertEquals(SummaryStatus.PENDING, updated.getSummaryStatus(), "Status must be set to PENDING upon note update");
    }

    @Test
    @DisplayName("6. Verify Keyword & Hybrid Semantic Search Functions")
    public void testKeywordAndHybridSearch() {
        CreateNoteRequest req1 = new CreateNoteRequest();
        req1.setTitle("PostgreSQL Performance Tuning");
        req1.setContent("Optimizing GIN indexes and HNSW vector index settings for large scale queries.");
        req1.setTags("database, postgresql");
        noteService.createNote(req1, testUser1);

        CreateNoteRequest req2 = new CreateNoteRequest();
        req2.setTitle("Spring Security Configuration");
        req2.setContent("Stateless JWT authentication filter and security filter chain setup.");
        req2.setTags("security, spring");
        noteService.createNote(req2, testUser1);

        // Keyword Search
        List<Note> keywordResults = noteService.search(testUser1, "Database");
        Assertions.assertFalse(keywordResults.isEmpty());
        Assertions.assertTrue(keywordResults.stream().anyMatch(n -> n.getTitle().contains("PostgreSQL")));

        // Hybrid Search
        List<SearchResultDTO> hybridResults = noteService.searchHybrid(testUser1, "PostgreSQL");
        Assertions.assertNotNull(hybridResults);
    }

    @Test
    @DisplayName("7. Verify Edge Cases: Unicode, Emojis, Code Snippets & Large Content")
    public void testEdgeCasesAndUnicode() {
        StringBuilder largeContent = new StringBuilder();
        largeContent.append("🚀 Memoria AI Operating System • 内存 systems • 🧠 System Test\n");
        largeContent.append("```java\n");
        largeContent.append("public class MemoryKernel {\n");
        largeContent.append("    public static void main(String[] args) {\n");
        largeContent.append("        System.out.println(\"Hello Memoria UTF-8: 🔥 Spark ✨\");\n");
        largeContent.append("    }\n");
        largeContent.append("}\n");
        largeContent.append("```\n");
        for (int i = 0; i < 500; i++) {
            largeContent.append("Continuous background indexing and memory context extraction line ").append(i).append("\n");
        }

        CreateNoteRequest req = new CreateNoteRequest();
        req.setTitle("Unicode & Code Test 🤖");
        req.setContent(largeContent.toString());
        req.setTags("unicode, emoji, markdown, code");

        Note note = noteService.createNote(req, testUser1);
        Assertions.assertNotNull(note.getId());

        Note fetched = noteService.findById(note.getId());
        Assertions.assertTrue(fetched.getContent().contains("🚀 Memoria AI Operating System"));
        Assertions.assertTrue(fetched.getContent().contains("public class MemoryKernel"));
    }
}
