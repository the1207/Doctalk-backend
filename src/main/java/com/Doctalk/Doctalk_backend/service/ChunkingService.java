package com.Doctalk.Doctalk_backend.service;

import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChunkingService {

    private static final int CHUNK_SIZE = 500;      // Nombre de mots par chunk
    private static final int OVERLAP = 50;          // Chevauchement entre les chunks

    public List<String> chunkText(String text) {
        List<String> chunks = new ArrayList<>();

        if (text == null || text.isEmpty()) {
            return chunks;
        }

        // Nettoyer le texte (supprimer les espaces multiples)
        String cleanedText = text.replaceAll("\\s+", " ").trim();

        // Séparer le texte en mots
        String[] words = cleanedText.split(" ");

        if (words.length <= CHUNK_SIZE) {
            chunks.add(cleanedText);
            return chunks;
        }

        // Découpage avec chevauchement
        int start = 0;
        while (start < words.length) {
            int end = Math.min(start + CHUNK_SIZE, words.length);
            StringBuilder chunk = new StringBuilder();

            for (int i = start; i < end; i++) {
                chunk.append(words[i]);
                if (i < end - 1) {
                    chunk.append(" ");
                }
            }

            chunks.add(chunk.toString());

            // Si on est arrivé à la fin, on sort
            if (end == words.length) {
                break;
            }

            // Avancer avec chevauchement
            start += CHUNK_SIZE - OVERLAP;
        }

        return chunks;
    }
}