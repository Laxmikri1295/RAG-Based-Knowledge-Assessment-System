package com.bookreaderai.backend.util;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Small utility to extract text from PDF and split into overlapping chunks.
 */
import org.springframework.stereotype.Component;

@Component
public class PdfChunker {

    public String extractTextFromPdf(Path pdfPath) throws IOException {
        try (PDDocument doc = PDDocument.load(Objects.requireNonNull(pdfPath).toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc); // raw text
        }
    }
    public String normalizeWhitespace(String s) {
        if (s == null) return "";
        return s.replaceAll("\\s+", " ").trim();
    }

    /**
     * Slice text into overlapping chunks.
     *
     * @param text     normalized text (prefer calling normalizeWhitespace first)
     * @param maxChars approximate maximum characters per chunk (e.g. 600-800)
     * @param overlap  number of characters to overlap between consecutive chunks (e.g. 100-200)
     * @return list of Chunk objects (index, startOffset, endOffset, text)
     */
    public List<Chunk> chunkText(String text, int maxChars, int overlap) {
        List<Chunk> chunks = new ArrayList<>();
        if (text == null || text.isBlank()) return chunks;

        int n = text.length();
        int start = 0;
        int idx = 0;

        // prefer to cut after at least half of maxChars
        int minCut = Math.max(1, maxChars / 2);

        while (start < n) {
            int endCandidate = Math.min(n, start + maxChars);

            // look backwards for a sentence boundary (., ?, !) between (start+minCut) .. endCandidate
            int cut = -1;
            int searchFrom = endCandidate;
            int searchTo = Math.max(start + minCut, start + 1);

            for (int i = searchFrom; i > searchTo; i--) {
                char c = text.charAt(i - 1);
                if (c == '.' || c == '!' || c == '?') {
                    // cut after punctuation
                    cut = i;
                    break;
                }
            }

            if (cut == -1) {
                // no punctuation found in the preferred window; cut at max length
                cut = endCandidate;
            }

            // create chunk (trim text to remove leading/trailing whitespace)
            String chunkText = text.substring(start, cut).trim();
            chunks.add(new Chunk(idx, start, cut, chunkText));

            // compute next start so we preserve overlap
            int nextStart = cut - overlap;
            // ensure we always move forward at least 1 char to avoid infinite loop
            if (nextStart <= start) {
                nextStart = start + 1;
            }
            start = Math.min(nextStart, n); // cap at n
            idx++;
        }

        return chunks;
    }

    /**
     * Simple immutable chunk representation.
     */
    public static class Chunk {
        private final int index;
        private final int startOffset;
        private final int endOffset;
        private final String text;

        public Chunk(int index, int startOffset, int endOffset, String text) {
            this.index = index;
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.text = text;
        }

        public int getIndex() {
            return index;
        }

        public int getStartOffset() {
            return startOffset;
        }

        public int getEndOffset() {
            return endOffset;
        }

        public String getText() {
            return text;
        }

        @Override
        public String toString() {
            String preview = text.length() > 120 ? text.substring(0, 120) + "..." : text;
            return String.format("Chunk[%d] offsets=(%d,%d) len=%d preview=\"%s\"",
                    index, startOffset, endOffset, text.length(), preview.replaceAll("\n", " "));
        }
    }
}
