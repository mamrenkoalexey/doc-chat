# doc-chat

Pet project. Chatbot that lets you upload documents/images and ask questions about them. Made it to learn Spring AI and vector search.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-brightgreen?logo=spring)
![Spring AI](https://img.shields.io/badge/Spring%20AI-1.1.6-brightgreen?logo=spring)
![PostgreSQL](https://img.shields.io/badge/pgvector-PostgreSQL%2017-blue?logo=postgresql)

## What it does

- Upload PDF, Word, Excel etc. or an image
- Choose which files to use in the conversation
- Ask a question — the app searches the docs and answers using GPT

Under the hood it's RAG — documents get split into chunks, embedded into vectors, stored in postgres with pgvector, then retrieved by similarity when you ask something.

## Stack

- **Java 21 + Spring Boot 3.5**
- **Spring AI 1.1.6** — embeddings, vector store, chat
- **PostgreSQL + pgvector** — vector storage
- **Apache Tika** — parses basically any document format
- **OpenAI** — `text-embedding-3-small` for embeddings, `gpt-4o-mini` for chat, Vision API for images

## Supported formats

Documents (Tika): PDF, DOCX, XLSX, PPTX, TXT, MD, CSV, HTML, RTF, ODT, EPUB and more

Images (GPT-4o Vision): PNG, JPG, WEBP, GIF, BMP, TIFF

## How to run

Need: Docker, OpenAI API key

**1. Clone**

```bash
git clone https://github.com/mamrenkoalexey/doc-chat.git
cd doc-chat
```

**2. Create `.env`**

```
OPENAI_API_KEY=sk-...
```

**3. Run**

```bash
docker-compose up --build
```

Open http://localhost:8080

---

## API

```
POST /documents   upload a file
GET  /documents   list files
POST /chat        ask a question
```