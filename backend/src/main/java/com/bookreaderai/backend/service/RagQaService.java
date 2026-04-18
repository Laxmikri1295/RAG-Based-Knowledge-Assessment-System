package com.bookreaderai.backend.service;

import com.bookreaderai.backend.util.OllamaEmbedder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.ollama.OllamaChatModel;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Retrieval-Augmented Generation (RAG) helper – fetch relevant chunks
 * from the `documents` table (pgvector) and delegate answer / quiz
 * generation to the Ollama chat model.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RagQaService {

    private final JdbcTemplate jdbcTemplate;
    private final OllamaEmbedder embedder;
    private final OllamaChatModel chatModel;

    /**
     * Answer a question about a particular book.
     *
     * @param bookName logical id used when the PDF was uploaded
     * @param question user question
     * @param topK     number of similar chunks to retrieve
     */
    public String answerQuestion(String bookName, String question, int topK) {
        // 1) embed the question
        float[] qVec = embedder.embedBatch(List.of(question)).get(0);

        // 2) similarity search in pgvector
        List<String> contexts = fetchSimilarChunks(bookName, qVec, topK);

        // 3) build prompt & ask LLM
        String contextBlock = String.join("\n---\n", contexts);
        String promptText = """
                You are a helpful assistant. Use the following context from the book to answer the user's question.
                If the answer is not contained in the context, say "I don't know".

                Context:
                %s

                Question: %s
                Answer:""".formatted(contextBlock, question);
        System.out.println("prompt for llm is " + promptText);
        System.out.println("context for the question is " + contextBlock);

        ChatResponse response = chatModel.call(new Prompt(promptText));
        return response.getResult().getOutput().getText();
    }

    /**
     * Generate a quiz (MCQ / short answer etc.) from the book content.
     *
     * @param bookName    logical id
     * @param instruction e.g. "Create 10 MCQ from chapter 8"
     * @param topK        number of chunks to feed as context
     */
    public String generateQuiz(String bookName, String instruction, int topK) {
        float[] vec = embedder.embedBatch(List.of(instruction)).get(0);
        List<String> contexts = fetchSimilarChunks(bookName, vec, topK);

        String contextBlock = String.join("\n---\n", contexts);
        String promptText = """
                You are a quiz generator. Use the following context from the book to fulfil the user's instruction.

                Context:
                %s

                Instruction: %s
                """.formatted(contextBlock, instruction);

        ChatResponse response = chatModel.call(new Prompt(promptText));
        return response.getResult().getOutput().getText();
    }

    /**
     * Generate a structured quiz (JSON array of MCQs).
     */
    public String generateStructuredQuiz(String bookName, String instruction, int topK) {
        float[] vec = embedder.embedBatch(List.of(instruction)).get(0);
        List<String> contexts = fetchSimilarChunks(bookName, vec, topK);
        
        log.info("generateStructuredQuiz: Retrieved {} chunks for docId='{}'", contexts.size(), bookName);

        String contextBlock = String.join("\n---\n", contexts);
        String promptText = """
                You are a quiz generator. Use the following context from the book to fulfil the user's instruction.
                You MUST return the output ONLY as a valid flat JSON array of objects. Do not include any markdown formatting like ```json.
                CRITICAL INSTRUCTION: DO NOT wrap the array inside another object or an array (e.g. no "questions" key). Return the flat array directly.
                Return an array like this:
                [
                  {
                    "question": "The question text",
                    "options": ["Option A", "Option B", "Option C", "Option D"],
                    "correctAnswer": "The exact string from options that is correct"
                  }
                ]

                Context:
                %s

                Instruction: %s
                """
                .formatted(contextBlock, instruction);

        ChatResponse response = chatModel.call(new Prompt(promptText));
        String text = response.getResult().getOutput().getText().trim();
        // Fallback: cleanup if LLM still prefixes with ```json
        if (text.startsWith("```json")) {
            text = text.replaceFirst("```json\\s*", "");
        }
        if (text.startsWith("```")) {
            text = text.replaceFirst("```\\s*", "");
        }
        if (text.endsWith("```")) {
            text = text.substring(0, text.length() - 3).trim();
        }
        
        text = text.trim();

        // Fallback: extract nested 'questions' array if LLM mistakenly wraps it
        if (text.contains("\"questions\":")) {
            int startIdx = text.indexOf("[", text.indexOf("\"questions\":"));
            int endIdx = text.lastIndexOf("]");
            if (text.endsWith("}")) {
                endIdx = text.lastIndexOf("]");
            } else if (text.endsWith("}]")) {
                endIdx = text.lastIndexOf("]", text.length() - 2);
            }
            if (startIdx != -1 && endIdx != -1 && startIdx < endIdx) {
                text = text.substring(startIdx, endIdx + 1);
            }
        }

        // Fallback: if LLM returned multiple objects without array brackets
        if (text.startsWith("{")) {
            int lastBrace = text.lastIndexOf("}");
            if (lastBrace != -1) {
                text = text.substring(0, lastBrace + 1); // trim off incomplete objects
            }
            // Insert commas between consecutive objects if missing
            text = "[" + text.replaceAll("\\}\\s*\\{", "}, {") + "]";
        }

        // Fallback: if text starts with [ but is missing ], due to token limits or odd generation
        if (text.startsWith("[") && !text.endsWith("]")) {
            int lastBrace = text.lastIndexOf("}");
            if (lastBrace != -1) {
                text = text.substring(0, lastBrace + 1) + "]";
            } else {
                text = text + "]"; 
            }
        }

        return text;
    }

    /**
     * Hit Postgres with a pgvector ANN search, return the chunk_text values.
     */
    /**
     * Retrieve the most similar chunks for a given docId. To make the API more
     * forgiving, if no rows are found for the provided id we automatically try
     * an alternate form with/without the “.pdf” suffix.
     *
     * E.g. if the user supplies "mybook.pdf" but the doc_id stored was
     * "mybook", we still return results.
     */
    private List<String> fetchSimilarChunks(String docId, float[] queryVec, int limit) {
        // first attempt – exact id
        List<String> chunks = queryChunks(docId, queryVec, limit);

        // fallback: strip or append ".pdf"
        if (chunks.isEmpty()) {
            String alt = docId.endsWith(".pdf")
                    ? docId.substring(0, docId.length() - 4)
                    : docId + ".pdf";
            if (!alt.equals(docId)) {
                chunks = queryChunks(alt, queryVec, limit);
            }
        }
        return chunks;
    }

    /**
     * Helper that executes the actual pgvector similarity SQL.
     */
    private List<String> queryChunks(String docId, float[] queryVec, int limit) {
        String sql = """
                SELECT chunk_text
                FROM documents
                WHERE LOWER(REPLACE(doc_id, ' ', '_')) = LOWER(REPLACE(?, ' ', '_'))
                ORDER BY embedding <=> ?::vector
                LIMIT ?
                """;
        String vecLiteral = asPgVectorLiteral(queryVec);
        return jdbcTemplate.query(sql, ps -> {
            ps.setString(1, docId);
            ps.setString(2, vecLiteral);
            ps.setInt(3, limit);
        }, (ResultSet rs, int rowNum) -> rs.getString("chunk_text"));
    }

    /**
     * '[0.1,0.2,0.3]' – textual representation pgvector expects.
     */
    private String asPgVectorLiteral(float[] vec) {
        StringBuilder sb = new StringBuilder(vec.length * 10);
        sb.append('[');
        for (int i = 0; i < vec.length; i++) {
            if (i > 0)
                sb.append(',');
            sb.append(vec[i]);
        }
        sb.append(']');
        return sb.toString();
    }
}
