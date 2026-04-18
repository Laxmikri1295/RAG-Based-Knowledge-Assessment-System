package com.bookreaderai.backend.service;

import com.bookreaderai.backend.util.OllamaEmbedder;
import com.bookreaderai.backend.util.PdfChunker;
import com.bookreaderai.backend.util.PdfChunker.Chunk;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.postgresql.util.PGobject;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service that:
 *  1. extracts & chunks PDF text
 *  2. embeds each chunk using Ollama
 *  3. persists chunks + vectors into the postgres/pgvector table `documents`
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentVectorService {

    private final PdfChunker pdfChunker;
    private final OllamaEmbedder embedder;
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * Full pipeline – extract → chunk → embed → store.
     *
     * @param pdfPath   path to a local PDF file
     * @param docId     logical id for the document (e.g. the book name)
     * @param metadata  key/value metadata to persist as jsonb
     */
    public void processAndStore(Path pdfPath, String docId, Map<String, String> metadata) throws IOException {
        log.info("Vector store pipeline started for docId={}", docId);

        // 1. read & normalise text
        String raw = pdfChunker.extractTextFromPdf(pdfPath);
        String normalised = pdfChunker.normalizeWhitespace(raw);

        // 2. chunk text
        List<Chunk> chunks = pdfChunker.chunkText(normalised, 700, 150);
        List<String> texts = chunks.stream()
                                   .map(Chunk::getText)
                                   .collect(Collectors.toList());
        log.info("Created {} chunks for docId={}", chunks.size(), docId);

        // 3. embed
        List<float[]> vectors = embedder.embedAll(texts, 16);
        if (vectors.size() != chunks.size()) {
            throw new IllegalStateException("Embedding count mismatch (" + vectors.size() + " vectors for "
                    + chunks.size() + " chunks)");
        }

        // 4. batch insert
        batchInsertChunks(docId, chunks, vectors, metadata);
        log.info("Vector store pipeline completed for docId={}", docId);
    }

    private void batchInsertChunks(String docId,
                                   List<Chunk> chunks,
                                   List<float[]> vectors,
                                   Map<String, String> metadata) {

        String sql = """
                INSERT INTO documents (doc_id, chunk_index, chunk_text, metadata, embedding)
                VALUES (?,?,?,?,?)
                """;

        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Chunk chunk = chunks.get(i);
                float[] vec = vectors.get(i);

                ps.setString(1, docId);
                ps.setInt(2, chunk.getIndex());
                ps.setString(3, chunk.getText());

                // jsonb metadata
                PGobject jsonObj = new PGobject();
                jsonObj.setType("jsonb");
                try {
                    jsonObj.setValue(objectMapper.writeValueAsString(metadata));
                } catch (IOException e) {
                    throw new SQLException("Failed serialising metadata json", e);
                }
                ps.setObject(4, jsonObj);

                // pgvector embedding
                PGobject vecObj = new PGobject();
                vecObj.setType("vector");
                vecObj.setValue(asPgVectorLiteral(vec));
                ps.setObject(5, vecObj);
            }

            @Override
            public int getBatchSize() {
                return chunks.size();
            }
        });
    }

    /**
     * Convert a float[] to the textual representation expected by pgvector: '[0.1,0.2,0.3]'.
     */
    private String asPgVectorLiteral(float[] vec) {
        StringBuilder sb = new StringBuilder(vec.length * 10);
        sb.append('[');
        for (int i = 0; i < vec.length; i++) {
            if (i > 0) sb.append(',');
            sb.append(Float.toString(vec[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}
