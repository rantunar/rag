# Document Loading, Chunking, Embedding & Retrieval Pipeline

This module provides a complete pipeline for:

- Loading documents from classpath
- Parsing them using Apache Tika
- Splitting them into semantic chunks
- Embedding the chunks
- Storing embeddings in PostgreSQL (pgvector)
- Retrieving relevant chunks using metadata filters
- Passing retrieved content to an LLM for contextual chat

