# Memoria

> **Your second brain, powered by AI.**

Memoria is an AI-powered personal knowledge management system built with **Spring Boot**, **PostgreSQL**, **pgvector**, and **Ollama**. It enables users to securely store notes, organize them with tags, search using both traditional keyword matching and semantic similarity, and automatically generate concise summaries using local AI models.

Unlike conventional note-taking applications, Memoria combines full-text search with vector embeddings to retrieve information based on meaning rather than exact wording. By running entirely on local AI models through Ollama, it also preserves user privacy while eliminating dependence on cloud-based AI services.

---

# Vision

Human memory is imperfect.

Important ideas become buried beneath hundreds of notes, documents, and fleeting thoughts. Traditional search relies on exact words, making it difficult to rediscover information when we remember only the concept.

Memoria aims to become an intelligent second brain capable of understanding what users mean, not just what they type.

The long-term goal is to build a personal knowledge system that remembers, organizes, summarizes, and retrieves information as naturally as human memory itself.

---

# Current Features

## Authentication

* Secure user registration
* BCrypt password hashing
* Spring Security integration
* Session-based authentication

## Notes

* Create notes
* Read notes
* Update notes
* Delete notes

## Tags

* Attach multiple tags to notes
* Automatic tag reuse
* Organized note categorization

## AI Embeddings

Every note automatically receives a semantic embedding generated using Ollama.

This allows Memoria to understand the meaning of notes rather than relying solely on keywords.

## Semantic Search

Searches notes using vector similarity powered by PostgreSQL's pgvector extension.

## Hybrid Search

Combines:

* PostgreSQL Full-Text Search
* Vector Similarity Search

to produce more relevant search results.

## AI Summaries

Every note is summarized automatically in the background using a locally running Ollama model.

Summaries are generated asynchronously without blocking user requests.

## Event-Driven Processing

Summary generation is triggered through Spring's event system after successful database commits, ensuring responsiveness and transactional safety.

---

# Technology Stack

### Backend

* Java 21
* Spring Boot 4
* Spring Security
* Spring Data JPA
* Hibernate ORM 7
* Maven

### Database

* PostgreSQL
* pgvector
* Full-Text Search (GIN Indexes)

### Artificial Intelligence

* Ollama
* nomic-embed-text
* llama3.2

### Development Tools

* IntelliJ IDEA
* VS Code
* Git
* Maven

---

# Architecture Overview

```text
Client

↓

Spring MVC

↓

Controllers

↓

Services

↓

Repositories

↓

PostgreSQL
     │
     ├── Full-text Search
     └── pgvector

↓

Spring Events

↓

Async Thread Pool

↓

Ollama

↓

Summary Update
```

---

# Highlights

* Local AI (No cloud dependency)
* Semantic search
* Hybrid ranking
* Event-driven architecture
* PostgreSQL vector database
* Asynchronous AI processing
* Clean layered architecture

---

# Running the Project

## Requirements

* Java 21
* Maven
* PostgreSQL
* pgvector extension
* Ollama

### Install Ollama Models

```bash
ollama pull nomic-embed-text
ollama pull llama3.2
```

### Start Ollama

```bash
ollama serve
```

### Start PostgreSQL

Ensure the `pgvector` extension is installed.

```sql
CREATE EXTENSION IF NOT EXISTS vector;
```

### Run the Application

```bash
./mvnw spring-boot:run
```

---

# Current Status

## Completed

* User Authentication
* CRUD Operations
* Tag Management
* PostgreSQL Integration
* Vector Embeddings
* Hybrid Search
* Local AI Summaries
* Event-Driven Background Processing

---

# Planned Features

* Document Uploads
* OCR
* PDF Parsing
* RAG-based Question Answering
* Knowledge Graphs
* Shared Workspaces
* Real-time Collaboration
* REST API
* Mobile Application
* Cloud Deployment
* Advanced AI Memory Retrieval

---

# Documentation

Additional documentation is available in the `docs/` directory.

* Project Overview
* Architecture
* Development Log
* Changelog

---

# Project Philosophy

Memoria is not intended to become another note-taking application.

Its objective is to become an intelligent memory system that helps users capture, understand, organize, and retrieve knowledge with the assistance of modern AI.

Every engineering decision—from asynchronous processing to local AI inference—has been made with scalability, privacy, and maintainability in mind.

---

# License

This project is currently under active development.

License information will be added before the first public release.
