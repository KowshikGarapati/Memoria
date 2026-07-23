# ARCHITECTURE.md

# Memoria Architecture

> **Version:** Backend Milestone (v0.1)

---

# Overview

Memoria follows a layered, event-driven architecture built around Spring Boot.

Each layer has a single responsibility, minimizing coupling while maximizing maintainability and scalability.

The architecture separates:

* HTTP handling
* Business logic
* Persistence
* AI workloads
* Background processing

This separation allows the system to evolve without large-scale refactoring.

---

# High-Level Architecture

```text
                    Browser
                       │
             Spring Security Filter
                       │
               Dispatcher Servlet
                       │
                  Controllers
                       │
                   Services
                       │
                Repositories
                       │
                 PostgreSQL
             ┌─────────┴──────────┐
             │                    │
        Full Text Search      pgvector
             │                    │
             └─────────┬──────────┘
                       │
                 Spring Events
                       │
              ThreadPool Executor
                       │
                   Ollama AI
                       │
              Summary Generation
                       │
                  Database Update
```

---

# Package Structure

```text
src/main/java

com.memoria.Memoria

├── config
│
├── controllers
│
├── dto
│
├── events
│
├── exception
│
├── listeners
│
├── models
│
├── repositories
│
├── security
│
├── services
│
└── MemoriaApplication
```

---

# Layer Responsibilities

## Controllers

Controllers expose HTTP endpoints.

Responsibilities:

* Receive requests
* Validate DTOs
* Delegate to services
* Return responses

Controllers never contain business logic.

---

## Services

The service layer contains the application's business rules.

Responsibilities include:

* Authentication
* CRUD operations
* Tag management
* Embedding generation
* Summary scheduling
* Search orchestration

Every significant workflow begins here.

---

## Repositories

Repositories communicate with PostgreSQL.

Responsibilities:

* Entity persistence
* JPQL queries
* Native SQL
* Hybrid search

Repositories contain no business logic.

---

## Models

The domain model represents the business entities.

Current entities:

* User
* Note
* Tag

Future entities may include:

* Workspace
* Attachment
* Conversation
* Reminder

---

# Database Design

## User

Stores:

* username
* email
* password

Relationship:

```text
User

1

↓

N

Notes
```

---

## Note

Stores:

* title
* content
* summary
* embedding
* timestamps

Each note belongs to one user.

Each note may contain multiple tags.

---

## Tag

Tags are shared across notes.

Relationship:

```text
Note

N

↔

N

Tag
```

---

# AI Architecture

Memoria currently uses two independent AI models.

## Embedding Model

```text
nomic-embed-text
```

Purpose:

Convert text into a 768-dimensional semantic vector.

---

## Summary Model

```text
llama3.2
```

Purpose:

Generate concise summaries.

---

# Embedding Pipeline

```
User creates note

↓

NoteService

↓

EmbeddingService

↓

Ollama

↓

768-dimensional vector

↓

Note Entity

↓

PostgreSQL vector column
```

Embeddings are generated synchronously before persistence.

Reason:

Hybrid search requires embeddings to exist immediately after insertion.

---

# Summary Pipeline

Summary generation is intentionally asynchronous.

```
User saves note

↓

Database Transaction

↓

Commit

↓

NoteSavedEvent

↓

Spring Listener

↓

Thread Pool

↓

Ollama

↓

Summary Generated

↓

REQUIRES_NEW Transaction

↓

Database Updated
```

Advantages:

* Fast HTTP responses
* Independent retries
* Better user experience
* Reduced transaction duration

---

# Event-Driven Design

Spring's event system decouples note persistence from AI workloads.

Instead of calling the AI service directly:

```
save()

↓

publishEvent()

↓

Listener

↓

Background Thread
```

Benefits:

* Loose coupling
* Improved scalability
* Easier testing
* Cleaner service layer

---

# Thread Pool

AI workloads execute inside a dedicated executor.

Purpose:

Prevent expensive inference operations from blocking request threads.

Typical configuration:

```
Core Threads

2

Maximum Threads

4

Queue Capacity

50
```

This isolates AI latency from normal application traffic.

---

# Search Architecture

Memoria implements Hybrid Search.

## Keyword Search

Powered by PostgreSQL Full-Text Search.

Uses:

```
to_tsvector()

plainto_tsquery()

ts_rank_cd()

ts_headline()
```

Excellent for exact wording.

---

## Semantic Search

Uses pgvector.

Computes cosine similarity.

```
embedding <=> queryVector
```

Excellent for conceptual matches.

---

## Hybrid Ranking

Final score:

```
Final Score

=

0.6 × Semantic Score

+

0.4 × Keyword Score
```

This balances precision with semantic understanding.

---

# Transaction Strategy

## CRUD

Standard transaction.

```
@Transactional
```

---

## Read Operations

```
@Transactional(readOnly = true)
```

Optimized for performance.

---

## Summary Updates

```
@Transactional(REQUIRES_NEW)
```

Reason:

Summary generation must remain independent from the original transaction.

---

# Security Architecture

Current implementation:

* Spring Security
* BCrypt
* Session authentication

Authorization:

Every operation verifies ownership.

```
User

↓

Owns?

↓

YES

↓

Continue

↓

NO

↓

UnauthorizedAccessException
```

Future roadmap:

* JWT
* Refresh Tokens
* OAuth2
* Role-based authorization

---

# Error Handling

Custom exceptions:

* UserAlreadyExistsException
* NoteNotFoundException
* UnauthorizedAccessException

Centralized exception handling keeps controllers clean and produces consistent responses.

---

# Design Decisions

## Why PostgreSQL?

* Mature ecosystem
* Excellent indexing
* Native Full-Text Search
* pgvector support

---

## Why Ollama?

* Local inference
* No API costs
* Privacy
* Offline capability

---

## Why Event-Driven Processing?

Direct AI calls during HTTP requests would:

* increase latency,
* hold database transactions open,
* reduce scalability.

Publishing an event after commit removes these problems.

---

## Why Hibernate Vector?

Early development revealed incompatibilities between PostgreSQL's custom `vector` type and Hibernate's default array handling.

The final solution adopted Hibernate 7's native vector support through:

* `hibernate-vector`
* `@JdbcTypeCode(SqlTypes.VECTOR)`
* `float[]`

This eliminated retrieval failures while preserving native pgvector functionality.

---

# Current Architecture Status

Implemented:

✔ Authentication

✔ CRUD

✔ Tags

✔ PostgreSQL

✔ pgvector

✔ Local Embeddings

✔ Local Summaries

✔ Hybrid Search

✔ Event System

✔ Async Processing

✔ Thread Pool

✔ Clean Layered Architecture

---

# Future Architecture

Planned additions include:

```
REST API

↓

React Frontend

↓

Redis Cache

↓

Document Processing

↓

OCR

↓

RAG Pipeline

↓

AI Chat

↓

Knowledge Graph

↓

Cloud Deployment

↓

Mobile Clients
```

The current architecture was intentionally designed to support these future components with minimal structural changes.

---

# Closing Notes

The architecture of Memoria emphasizes maintainability, scalability, and separation of concerns.

Every subsystem—from AI inference to persistence and search—operates independently through clearly defined interfaces, making the project easier to extend, test, and evolve.

As the project grows into a complete personal knowledge platform, this architecture provides a stable foundation capable of supporting significantly more advanced AI features without requiring fundamental redesign.
