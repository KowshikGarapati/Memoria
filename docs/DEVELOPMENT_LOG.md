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