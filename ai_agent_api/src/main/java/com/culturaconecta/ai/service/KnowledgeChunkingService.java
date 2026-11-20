package com.culturaconecta.ai.service;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class KnowledgeChunkingService {

    private static final int CHUNK_SIZE = 150;
    private static final int OVERLAP = 30;

    public List<String> split(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException(
                    "O documento não pode estar vazio."
            );
        }

        String[] words = content.trim().split("\\s+");
        List<String> chunks = new ArrayList<>();

        for (int start = 0; start < words.length; start += CHUNK_SIZE - OVERLAP) {
            int end = Math.min(start + CHUNK_SIZE, words.length);

            chunks.add(
                    String.join(" ",
                            Arrays.copyOfRange(words, start, end))
            );

            if (end == words.length) {
                break;
            }
        }

        return chunks;
    }
}

