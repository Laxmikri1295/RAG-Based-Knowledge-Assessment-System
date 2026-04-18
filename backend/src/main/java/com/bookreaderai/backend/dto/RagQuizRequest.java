package com.bookreaderai.backend.dto;

/**
 * Request body for generating a quiz from a book via RAG.
 *
 * @param bookName    logical id (same as doc_id)
 * @param instruction instruction to the AI, e.g. "Create 10 MCQ from chapter 8"
 */
public record RagQuizRequest(String bookName, String instruction) { }
