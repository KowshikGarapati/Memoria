# DEVELOPMENT_LOG.md

# Memoria Development Log

> A chronological engineering journal documenting the evolution of the Memoria backend.

---

# Project Timeline

**Project Start**

Early backend development began with the objective of building an intelligent note management platform powered by Artificial Intelligence rather than a conventional CRUD application.

From the beginning, the goal was not merely to store notes, but to make them searchable by meaning, automatically summarized, and extensible for future AI capabilities.

---

# Milestone 1 — Foundation

## Objective

Establish the basic backend architecture.

### Completed

* Spring Boot project initialization
* PostgreSQL integration
* Project structure
* Layered architecture
* Entity modelling

### Lessons

Investing time in clean architecture before adding features significantly reduced complexity later in development.

---

# Milestone 2 — Authentication

## Objective

Create a secure user system.

### Completed

* Registration
* Login
* BCrypt password hashing
* Spring Security configuration
* Ownership validation

### Challenges

Correctly separating authentication from authorization required several iterations before the design became sufficiently clean.

---

# Milestone 3 — Note Management

## Objective

Implement complete CRUD functionality.

### Completed

* Create notes
* Read notes
* Update notes
* Delete notes
* Tag management

### Design Decision

Business rules were intentionally kept inside the service layer while controllers remained thin request handlers.

This decision simplified testing and future feature additions.

---

# Milestone 4 — Semantic Search Begins

## Objective

Move beyond keyword search.

The plan was to represent every note as a semantic vector using embedding models.

This marked the beginning of Memoria's transition from a traditional note application into an AI-powered knowledge system.

---

# Engineering Challenge — PostgreSQL pgvector

## Initial Goal

Store a 768-dimensional embedding for every note.

The first implementation generated embeddings successfully using Ollama.

The vectors appeared correct.

Everything suggested success.

Until retrieval.

---

## The First Symptoms

Saving notes worked.

Creating embeddings worked.

Database inserts worked.

Yet reading notes immediately failed.

Hibernate consistently threw:

```text
org.postgresql.util.PSQLException:
No results were returned by the query.
```

Initially, the error strongly suggested that the SQL query itself was returning no rows.

This assumption proved to be incorrect.

---

## First Investigation

Several possibilities were explored:

* Incorrect SQL
* Missing embeddings
* Repository bugs
* Transaction timing
* PostgreSQL extension issues

None explained the behaviour.

The database clearly contained valid vectors.

---

## Temporary Workaround

The embedding field was temporarily marked as:

```java
@Transient
private double[] embedding;
```

This eliminated the retrieval exception.

However, it introduced a much larger problem.

Embeddings were no longer persisted at all.

Semantic search became impossible.

The workaround merely hid the underlying issue.

---

## Second Investigation

Logging was expanded throughout the application.

Embedding generation confirmed:

* vectors were generated,
* vectors were attached to entities,
* vectors were saved.

Database inspection confirmed:

* embedding column contained data.

The failure therefore occurred **after persistence**.

Attention shifted toward entity retrieval.

---

## Root Cause Discovery

After tracing Hibernate and PostgreSQL JDBC behaviour, the true problem emerged.

Hibernate interpreted:

```java
double[]
```

as a standard SQL array.

The PostgreSQL pgvector extension, however, stores vectors using its own custom SQL type:

```text
vector
```

These are fundamentally different.

During entity retrieval Hibernate attempted to deserialize the custom vector using its generic SQL array handler.

Internally the PostgreSQL JDBC driver attempted to retrieve array metadata.

Since pgvector is **not** a SQL array, the lookup failed, producing:

```text
No results were returned by the query.
```

The query itself had never been the problem.

The failure occurred during JDBC type conversion.

---

## Final Solution

The project migrated to Hibernate 7's native vector support.

Changes included:

* hibernate-vector dependency
* VectorJdbcType
* SqlTypes.VECTOR
* float[] representation

The entity became:

```java
@JdbcTypeCode(SqlTypes.VECTOR)
@Array(length = 768)
@Column(columnDefinition = "vector(768)")
private float[] embedding;
```

Immediately afterward:

* embeddings persisted correctly,
* retrieval succeeded,
* semantic search became fully operational.

One of the longest-running issues in the project had been resolved.

---

# Milestone 5 — Hybrid Search

## Objective

Combine traditional keyword search with semantic retrieval.

Implementation combined:

* PostgreSQL Full-Text Search
* pgvector cosine similarity

Ranking formula:

```text
Final Score

=

0.6 × Semantic Score

+

0.4 × Keyword Score
```

This significantly improved search quality by balancing exact wording with conceptual similarity.

---

# Milestone 6 — AI Summaries

## Original Design

Summaries were initially generated using Anthropic.

The abstraction layer:

```text
AiService
```

was intentionally introduced so that the implementation could later change without affecting business logic.

---

## Unexpected Obstacle

During development the Anthropic API quota was exhausted.

Rather than treating this as a temporary inconvenience, the decision was made to remove the cloud dependency entirely.

---

## Migration to Ollama

Anthropic was replaced with local inference.

Implementation:

* OllamaAiServiceImpl
* llama3.2

Advantages:

* zero API cost,
* offline capability,
* complete privacy,
* faster experimentation.

Because the project already depended upon the AiService abstraction, the migration required minimal architectural changes.

This validated the original design decision.

---

# Milestone 7 — Event-Driven Processing

Summary generation was intentionally separated from the main transaction.

Workflow:

```text
Save Note

↓

Commit Transaction

↓

Publish Event

↓

Background Thread

↓

Generate Summary

↓

Update Database
```

Benefits:

* faster HTTP responses,
* improved scalability,
* cleaner transactions,
* independent retries.

---

# Current Backend Status

Completed:

✓ Authentication

✓ Authorization

✓ CRUD

✓ Tag Management

✓ PostgreSQL

✓ pgvector

✓ Hibernate Vector Integration

✓ Embedding Generation

✓ Semantic Search

✓ Hybrid Search

✓ Local AI Summaries

✓ Event System

✓ Async Processing

---

# Lessons Learned

Several important engineering lessons emerged during development.

### Never trust the first interpretation of an error.

The pgvector issue appeared to be a SQL failure.

It was actually a JDBC type conversion problem.

---

### Temporary workarounds should remain temporary.

Using @Transient removed the exception but silently disabled one of the application's core features.

---

### Programming to interfaces pays off.

The migration from Anthropic to Ollama required replacing only one service implementation because the application depended on an abstraction rather than a concrete provider.

---

### Logging is a debugging tool, not merely an operational tool.

Detailed logging transformed a seemingly random Hibernate failure into a reproducible, understandable problem.

---

# Looking Ahead

With the backend foundation complete, future development will focus on:

* React frontend
* JWT authentication
* REST API
* Retrieval-Augmented Generation (RAG)
* AI conversations over personal notes
* Document ingestion
* Cloud deployment

The backend now provides a stable platform for these future milestones.

---

# Closing Reflection

The development of Memoria has consistently reinforced one principle:

The most difficult problems were rarely caused by missing code.

They were caused by incorrect assumptions.

Every major breakthrough—from vector persistence to AI migration—came from questioning those assumptions, investigating the system layer by layer, and allowing evidence rather than intuition to guide the solution.

This document exists not only to record what was built, but also to preserve the reasoning that shaped the project.

---
# 23/07/20226 thursday

We have encoutnered a weird situation in our final testing before we pulish the mvp into ou git repo, which is, our Ollama server has went rogue and we couldn't find why ot happened. it prompts that it is up and running but the database shows otherwise. the database isn't recording any data regarding embeddings or any summary of any kind. when i looked at the logs to looks for the cause, i've found that the Ollama server is the problem, nothing else. so, I've assigned Antigravity CLI a task to check for the root cause and it came up with this :

-------------------------------------------------------------------------------------------------------
### 🔍 Root Cause of the Initial Connection Refusal                                                                                                                                                                     
                                                                                                                                                                                                                         
  On Windows, localhost can attempt to resolve to IPv6 (::1:11434) first before falling back to IPv4. Because the local Ollama daemon binds specifically to IPv4 127.0.0.1:11434, Spring's WebClient was receiving       
  Connection Refused on ::1.                                                                                                                                                                                             
                                                                                                                                                                                                                         
  ### The Fix Applied:                                                                                                                                                                                                   
                                                                                                                                                                                                                         
  In application.properties, we updated the Ollama URL to target IPv4 explicitly:                                                                                                                                        
                                                                                                                                                                                                                         
    application.ai.ollama.url=http://127.0.0.1:11434                                                                                                                                                                     
    ──────                                                                                                                                                                                                               
  ## 📊 Live Verification Log Evidence                                                                                                                                                                                   
                                                                                                                                                                                                                         
  During test execution, local Ollama models (nomic-embed-text and llama3.2) were invoked live, and real AI summaries were generated and persisted to your PostgreSQL database.                                          
                                                                                                                                                                                                                         
  ### 1. Live Vector Embedding Generation & Binding:                                                                                                                                                                     
                                                                                                                                                                                                                         
    TRACE org.hibernate.orm.jdbc.bind : binding parameter (3:VECTOR) <- [[0.123, 0.0, ..., 0.987]]                                                                                                                       
    Hibernate:                                                                                                                                                                                                           
        insert into note (content, created_at, embedding, ...) values (?, ?, cast(? as vector), ...)                                                                                                                     
                                                                                                                                                                                                                         
  ### 2. Live Ollama Text Summarization Response (Model: llama3.2):                                                                                                                                                      
                                                                                                                                                                                                                         
    TRACE org.hibernate.orm.jdbc.bind : binding parameter (4:VARCHAR) <- [The content was significantly updated for testing purposes to observe and evaluate its update behavior. This update aimed to refine the        
  system's performance and functionality. The changes made were intended to ensure seamless updates and improve overall user experience.]                                                                                
    TRACE org.hibernate.orm.jdbc.bind : binding parameter (6:VARCHAR) <- [SUCCESS]                                                                                                                                       
    INFO  c.m.Memoria.services.NoteSummaryService : Updated database status to SUCCESS for note ID: 49                                                                                                                   
    INFO  c.m.Memoria.services.NoteSummaryService : Summary persisted successfully for note ID: 49                                                                                                                       
    ──────                                                                                                                                                                                                               
  ## 🏆 Final QA Test Suite Results                                                                                                                                                                                      
                                                                                                                                                                                                                         
    [INFO] Results:                                                                                                                                                                                                      
    [INFO] Tests run: 7, Failures: 0, Errors: 0, Skipped: 0                                                                                                                                                              
    [INFO] ------------------------------------------------------------------------                                                                                                                                      
    [INFO] BUILD SUCCESS                                                                                                                                                                                                 
    [INFO] ------------------------------------------------------------------------                                                                                                                                      
                                                                                                                                                                                                                         
  • ✅ Vector Storage & Retrieval: 768-dimensional float arrays created, stored in PostgreSQL, and retrieved cleanly.                                                                                                    
  • ✅ Local Ollama Summarization: Real summaries generated asynchronously via llama3.2 and saved with status SUCCESS.                                                                                                   
  • ✅ Resilience: Fallbacks prevent server crashes if Ollama is paused or busy.                                                                                                                                         
                                                                                                                                                                                                                         
  Your Memoria AI Personal Memory System backend is fully operational and functioning exactly as intended!

  -----------------------------------------------------------------------------------------------------

Apparently there has been a slight technical incompatibility that ran the Ollama server in IPv6 instead of IPv4 sa we need it. It is caused because the act of shutting down the computer made the system reset for all the local machines and therefore, occured this error. 

## well, I will have a keen note of it and will not get stuck in a situstion of this kind

## 24/07/2026 friday
# -------------------------------------- PHASE 3 DEVELOPMENT ---------------------------------------

In the phase three, I am going to focus on working on :
    
    1. Better Search
    2. Pagination, Filters, Sorting
    3. Markdown Note Experience
    4. Testing
    5. Documentation
    6. AI Chat

## PHASE 3.1 — Better Search Quality (Commit 1)

The objective of this commit was not to immediately write SQL, but to first establish a clean architecture for the upcoming hybrid search engine.

Rather than allowing the repository, controller, and service layers to exchange primitive values directly, I designed a dedicated search domain that separates the request, response, configuration, and persistence concerns.

The first step was introducing strongly typed search models.

Implemented:

• SearchRequest
    - Encapsulates every user-controlled search parameter.
    - Query text
    - Tag filters
    - Date range filters
    - Pagination
    - Sort mode

• SearchSort
    - Introduced an enum instead of raw String values.
    - Supported:
        - RELEVANCE
        - NEWEST
        - OLDEST

• SearchResultItem
    - Future-proof DTO representing a ranked search result.
    - Includes:
        - id
        - title
        - content
        - summary
        - timestamps
        - tags
        - ranking score
        - highlighted snippet

To avoid unnecessary database queries, a type-safe projection interface (SearchResultProjection) was introduced.

Instead of loading Note entities and then issuing additional queries for tags, PostgreSQL now aggregates tag names using string_agg(), allowing Spring Data JPA to map everything directly into projection interfaces.

This design completely avoids the classic N+1 query problem during search.

Search ranking weights were also externalized into SearchProperties using @ConfigurationProperties.

Instead of hardcoding ranking constants inside SQL, every field weight can now be adjusted from application.properties.

Current weights:

• Title ............ 1.0
• Summary ......... 0.8
• Tags .............. 0.5
• Content ......... 0.2

Search blending coefficients were also externalized.

Keyword Search : 40%

Semantic Search : 60%

This means future tuning requires configuration changes rather than code modifications.

Finally, MemoriaApplication was updated to register SearchProperties using @EnableConfigurationProperties, keeping configuration clean without introducing unnecessary @Component annotations.

Build Status

✓ BUILD SUCCESS

Outcome

Commit 1 established the architectural foundation required for the hybrid search engine while maintaining strong separation of concerns between DTOs, projections, configuration, and persistence.

## PHASE 3.1 — Better Search Quality (Commit 2)

With the search architecture established, the second commit focused entirely on the database engine responsible for executing hybrid search.

Rather than performing multiple repository calls and merging results inside Java, the goal was to let PostgreSQL perform the ranking in a single optimized query.

The NoteRepository was redesigned around a native SQL query using Common Table Expressions (CTEs).

The first stage constructs a weighted search document by combining:

• Title
• Summary
• Tags
• Content

Each field receives a different importance using PostgreSQL's setweight() function.

Weight Categories

A → Title

B → Summary

C → Tags

D → Content

The resulting weighted document is ranked using ts_rank_cd(), producing a keyword relevance score.

Semantic search is then incorporated using pgvector cosine similarity.

The final ranking formula combines both scores.

Final Score

=

Keyword Score × Keyword Blend

+

Semantic Score × Vector Blend

The ranking coefficients are supplied dynamically from SearchProperties rather than being embedded into SQL.

Additional improvements include:

• Native PostgreSQL highlighting using ts_headline()
• Dynamic sorting
    - Relevance
    - Newest
    - Oldest
• Tag aggregation using string_agg()
• Window function COUNT(*) OVER() for pagination metadata
• Spring Data Pageable integration for automatic LIMIT and OFFSET generation

Most importantly, every result is projected directly into SearchResultProjection, eliminating unnecessary entity loading and secondary database queries.

Build Status

✓ BUILD SUCCESS

Outcome

The database now performs weighted full-text search, semantic similarity ranking, pagination, sorting, highlighting, and total result counting in a single optimized query.

## PHASE 3.1 — Better Search Quality (Commit 3)

After completing the database engine, the next objective was to introduce a dedicated service layer capable of orchestrating every component involved in hybrid search.

A new SearchService interface was introduced to provide a single entry point for all search operations.

Its implementation, NoteSearchServiceImpl, became responsible for coordinating every stage of the search pipeline.

Workflow

Search Request

↓

Normalize Query

↓

Generate Embedding Vector

↓

Read Search Configuration

↓

Execute Repository Query

↓

Map Projection

↓

Return Paginated DTOs

The service performs several important responsibilities.

• Normalizes user queries.
• Generates embedding vectors using EmbeddingService.
• Falls back gracefully whenever embeddings cannot be generated.
• Reads ranking weights from SearchProperties.
• Executes the repository query.
• Converts SearchResultProjection into SearchResultItem DTOs.
• Returns results using Spring Data PageImpl.

To simplify interaction with imperative Spring MVC code, EmbeddingService was extended with a synchronous wrapper (getEmbeddingSync()) while preserving its reactive implementation internally.

This keeps reactive complexity isolated from the rest of the application.

Another important architectural improvement was replacing the legacy search implementation inside NoteController.

The Thymeleaf interface now communicates exclusively with SearchService, ensuring both the future REST API and the web interface share the exact same search pipeline.

This eliminates duplicated search logic and establishes a single source of truth for search behaviour.

Build Status

✓ BUILD SUCCESS

Outcome

The search system now follows a layered architecture where controllers delegate to a dedicated SearchService, the service coordinates embeddings and ranking configuration, and the repository focuses solely on optimized data retrieval.

## PHASE 3.1 — Better Search Quality (Commit 3.1)

During end-to-end testing, an unexpected issue appeared.

Regardless of the search query, a testing note named "Vector Test Note" continuously appeared in the results with a displayed relevance of NaN%.

The behaviour clearly indicated that candidate filtering and score calculation were not behaving as intended.

After tracing the execution path through PostgreSQL, pgvector, and the service layer, three independent issues were identified.

Issue 1 — Overly Permissive SQL Filtering

The repository query accepted every note whenever a query vector existed.

This caused unrelated notes to bypass filtering despite having neither keyword matches nor semantic similarity.

The WHERE clause was redesigned so that search results must satisfy at least one genuine matching condition.

Issue 2 — NaN Score Propagation

Invalid or missing vector values occasionally produced IEEE-754 NaN scores.

These values propagated into the DTO layer and ultimately appeared in the user interface as "NaN% Match".

Score sanitization was introduced before mapping projections into DTOs, ensuring invalid values safely fall back to zero.

Issue 3 — Legacy Controller Pipeline

The Thymeleaf controller was still invoking the legacy search implementation.

The controller was updated to delegate all requests to SearchService, guaranteeing identical behaviour between the web interface and the future REST API.

Verification

• BUILD SUCCESS
• Integration Tests Passed
• Unrelated notes no longer appear in results.
• Invalid NaN scores eliminated.
• Search ranking behaves consistently.

Outcome

The hybrid search engine reached a stable production-ready state.

Search results are now filtered correctly, relevance scores remain valid, and every user-facing search request passes through the same unified search pipeline.