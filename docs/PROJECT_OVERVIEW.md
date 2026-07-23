# PROJECT_OVERVIEW.md

# Memoria — Project Overview

## Project Information

**Project Name:** Memoria

**Version:** 0.1 (Backend Milestone)

**Project Type:** AI-Powered Personal Knowledge Management System

**Primary Language:** Java

**Framework:** Spring Boot 4

**Status:** Active Development

---

# 1. Introduction

Memoria is an intelligent note management platform designed to function as a user's "second brain."

Instead of merely storing notes, Memoria enriches every piece of information with artificial intelligence, allowing users to search by meaning, automatically generate summaries, and retrieve knowledge far more naturally than traditional note-taking applications.

The project combines modern backend engineering with recent advances in local Large Language Models (LLMs) to create an extensible foundation for future AI-powered productivity tools.

Unlike cloud-dependent AI applications, Memoria prioritizes privacy by running all AI workloads locally through Ollama.

---

# 2. Motivation

Modern users consume an enormous amount of information every day.

Books.

Articles.

Research papers.

Meeting notes.

Ideas.

Tasks.

Traditional note-taking systems become increasingly difficult to search because they rely almost entirely on keyword matching.

Human memory, however, does not work through exact keywords.

We remember concepts.

Ideas.

Relationships.

Memoria attempts to bridge this gap through semantic search powered by vector embeddings.

---

# 3. Objectives

The primary objectives of Memoria are:

* Secure personal knowledge storage
* Intelligent semantic retrieval
* Automatic AI-generated summaries
* Fast hybrid search
* Privacy-first local AI inference
* Scalable backend architecture
* Clean and maintainable software design

---

# 4. Core Features

## Authentication

Users can securely register and log into the platform.

Security features include:

* BCrypt password hashing
* Spring Security
* Session authentication
* User isolation

Every note belongs exclusively to its owner.

---

## Notes

Each note contains:

* Title
* Content
* Tags
* Summary
* Embedding Vector
* Timestamps

CRUD operations are fully implemented.

---

## Tag System

Notes may contain multiple tags.

Features include:

* Automatic tag creation
* Existing tag reuse
* Many-to-many relationships
* Efficient retrieval

---

## Semantic Embeddings

Every note is transformed into a 768-dimensional embedding vector using the Ollama model:

```
nomic-embed-text
```

These vectors capture the semantic meaning of each note.

Instead of storing only text, Memoria stores mathematical representations of meaning.

---

## AI Summarization

Every newly created or updated note automatically enters the summarization pipeline.

Summary generation:

* executes asynchronously,
* does not block user requests,
* updates the database after completion.

Current model:

```
llama3.2
```

---

## Hybrid Search

Memoria combines two independent search systems.

### Keyword Search

Powered by PostgreSQL Full-Text Search.

Excellent for exact terms.

---

### Semantic Search

Powered by PostgreSQL pgvector.

Excellent for conceptual similarity.

---

### Hybrid Ranking

Both scores are combined into a weighted ranking system to provide more accurate search results.

---

# 5. Technology Stack

## Backend

* Java 21
* Spring Boot 4
* Spring MVC
* Spring Security
* Spring Data JPA
* Hibernate ORM 7

---

## Database

* PostgreSQL
* pgvector Extension
* GIN Full-Text Indexes

---

## AI

Local Models via Ollama

Embedding Model

```
nomic-embed-text
```

Summary Model

```
llama3.2
```

---

## Build Tools

* Maven
* Git

---

## Development Environment

* IntelliJ IDEA
* VS Code

---

# 6. System Architecture

The project follows a layered architecture.

```
Client

↓

Controllers

↓

Services

↓

Repositories

↓

PostgreSQL
```

Each layer has a single responsibility.

Controllers never communicate directly with repositories.

Business logic exists exclusively inside the service layer.

---

# 7. Database Design

Primary entities:

User

↓

Note

↓

Tag

Relationships:

User

```
1 -----> N
```

Notes

Tags

```
N <----> N
```

Each Note additionally stores:

* summary
* summaryStatus
* summaryGeneratedAt
* embedding

---

# 8. AI Processing Pipeline

When a note is created:

1. User submits note.

2. Controller validates request.

3. Service generates embedding.

4. Note is saved.

5. Transaction commits.

6. Spring publishes NoteSavedEvent.

7. Background thread begins summary generation.

8. Ollama generates summary.

9. Database is updated.

This architecture guarantees fast HTTP responses.

---

# 9. Event-Driven Design

Summary generation is intentionally decoupled from the main transaction.

Advantages:

* Faster requests
* Better scalability
* Cleaner architecture
* Retry capability
* Isolation of failures

---

# 10. Security

Current implementation includes:

* BCrypt passwords
* User authorization
* Ownership validation
* Transactional services

Future improvements:

* JWT Authentication
* Refresh Tokens
* Rate Limiting
* CSRF Strategy
* OAuth2

---

# 11. Performance Considerations

Current optimizations include:

* EntityGraph for tag loading
* Hybrid SQL queries
* Async AI processing
* PostgreSQL vector indexes
* Full-text search indexes

---

# 12. Current Project Status

Completed

✓ Authentication

✓ CRUD

✓ Tags

✓ Embeddings

✓ PostgreSQL pgvector

✓ Hybrid Search

✓ Ollama Summaries

✓ Event System

✓ Async Processing

---

# 13. Future Roadmap

Phase 2

* React Frontend
* REST API
* JWT Authentication

Phase 3

* Document Uploads
* OCR
* PDF Parsing

Phase 4

* Retrieval-Augmented Generation (RAG)
* AI Chat over Personal Notes
* Knowledge Graphs

Phase 5

* Mobile Application
* Cloud Deployment
* Team Collaboration

---

# 14. Design Philosophy

Several principles guided the development of Memoria:

* Simplicity before complexity.
* Reliability before optimization.
* Local AI before cloud dependency.
* Meaning-based retrieval over keyword-only search.
* Clean architecture over quick implementation.

Every major feature was designed with extensibility in mind so that future AI capabilities can be integrated without redesigning the application's core.

---

# 15. Conclusion

Memoria is more than a note-taking application.

It is the foundation of an intelligent memory system.

By combining traditional software engineering with modern AI techniques such as embeddings, semantic search, and local language models, the project aims to evolve into a platform capable of helping users store, understand, and retrieve knowledge in a way that closely resembles human memory.

The backend milestone establishes a stable architecture upon which future frontend development, advanced AI features, and public deployment will be built.
