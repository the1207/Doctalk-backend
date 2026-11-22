# DocTalk Backend — RAG API for Hybrid AI Study Assistant

> Spring Boot backend for the [DocTalk](https://github.com/the1207/Doctalk-frontend) hybrid AI application — **#BuildHybridAI Hackathon 2026**

This backend handles document ingestion, vector embeddings, and semantic search (RAG) for real documents uploaded by users. The frontend uses Firebase AI Logic for inference; this backend supplies the retrieved context.

---

## What It Does

- **Document upload** — Accepts PDF and DOCX files, extracts text
- **Chunking & embedding** — Splits documents into chunks, generates embeddings via Gemini Embedding API
- **Vector search** — Stores embeddings in PostgreSQL with pgvector, finds the most relevant chunks for a query
- **RAG endpoint** — Returns AI-generated answers grounded in the document chunks
- **User auth** — JWT-based authentication

---

## Firebase AI Logic Connection

The frontend's [`FirebaseAiService`](https://github.com/the1207/Doctalk-frontend/blob/main/src/app/core/services/firebase-ai.service.ts) calls this backend to retrieve relevant document chunks, then passes them as context to Firebase AI Logic for hybrid inference (on-device or cloud).

Flow for a real document:
1. User asks a question on the frontend
2. Frontend calls `POST /api/chat` with the question and document ID
3. Backend embeds the question, searches pgvector for relevant chunks
4. Backend calls Gemini (cloud) with the chunks as context
5. Response + sources returned to frontend

---

## Tech Stack

- Java 21
- Spring Boot 4
- PostgreSQL + pgvector
- Apache PDFBox (PDF parsing)
- Apache POI (DOCX parsing)
- Gemini Embedding API (text-embedding-004)
- JWT authentication

---

## Getting Started

### Prerequisites
- Java 21+
- Maven 3.9+
- PostgreSQL with pgvector extension

### Setup

```bash
# Clone
git clone https://github.com/the1207/Doctalk-backend
cd Doctalk-backend

# Set environment variables
export GEMINI_API_KEY=your_gemini_api_key
export JWT_SECRET=your_jwt_secret

# Enable pgvector in PostgreSQL
psql -U postgres -c "CREATE EXTENSION IF NOT EXISTS vector;"
psql -U postgres -c "CREATE DATABASE doctalk;"

# Run
mvn spring-boot:run
```

API available at `http://localhost:8080`

### Key endpoints

| Method | URL | Description |
|--------|-----|-------------|
| POST | `/api/auth/register` | Register |
| POST | `/api/auth/login` | Login |
| POST | `/api/documents/upload` | Upload document |
| GET | `/api/documents` | List documents |
| POST | `/api/chat` | Ask a question (RAG) |
| GET | `/api/chat/conversations` | List conversations |

---

## License

Apache 2.0 — see [LICENSE](LICENSE)
