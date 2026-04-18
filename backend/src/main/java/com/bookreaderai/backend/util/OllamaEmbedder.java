package com.bookreaderai.backend.util;

import org.springframework.stereotype.Service;
import org.springframework.ai.embedding.EmbeddingModel;    // generic interface
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class OllamaEmbedder {

    private final EmbeddingModel embeddingModel;

    public OllamaEmbedder(EmbeddingModel embeddingModel) {
        this.embeddingModel = Objects.requireNonNull(embeddingModel, "embeddingModel");
    }

    /** Embed a single batch (delegates to the model). Expects texts.size() <= model capacity. */
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();
        return embeddingModel.embed(texts);
    }

    /**
     * Embed a large list of texts by slicing into batches of size `batchSize`.
     * Returns vectors in the same order as texts.
     */
    public List<float[]> embedAll(List<String> texts, int batchSize) {
        if (texts == null || texts.isEmpty()) return Collections.emptyList();
        List<float[]> all = new ArrayList<>(texts.size());
        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(texts.size(), i + batchSize);
            List<String> slice = texts.subList(i, end);
            List<float[]> vecs = embedBatch(slice);
            all.addAll(vecs);
        }
        return all;
    }
}

