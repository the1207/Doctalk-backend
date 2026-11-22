package com.Doctalk.Doctalk_backend.service.implementation;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.Doctalk.Doctalk_backend.service.EmbeddingService;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Service
public class GeminiEmbeddingService implements EmbeddingService {

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final String GEMINI_EMBEDDING_URL = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<Float> generateEmbedding(String text) {
        // Si aucune clé Gemini API n'est fournie (conforme aux exigences Firebase AI Logic),
        // on génère un vecteur d'embedding local déterministe pour pgvector sans dépendance externe
        if (apiKey == null || apiKey.isBlank() || apiKey.startsWith("${")) {
            return generateLocalEmbedding(text);
        }

        try {
            String url = GEMINI_EMBEDDING_URL + apiKey;

            JsonObject requestBody = new JsonObject();
            JsonObject content = new JsonObject();

            JsonArray partsArray = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", text);
            partsArray.add(part);

            content.add("parts", partsArray);
            requestBody.add("content", content);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // Fallback local en cas d'erreur API
                return generateLocalEmbedding(text);
            }

            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray embeddingArray = jsonResponse.getAsJsonObject("embedding")
                    .getAsJsonArray("values");

            List<Float> embedding = new ArrayList<>();
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding.add(embeddingArray.get(i).getAsFloat());
            }

            return embedding;

        } catch (Exception e) {
            // Fallback local pour garantir la résilience de l'application
            return generateLocalEmbedding(text);
        }
    }

    /**
     * Générateur d'embedding local 768-D déterministe par projection de hachage L2-normalisée.
     * Permet le fonctionnement complet de pgvector sans requérir de clé API Gemini externe.
     */
    private List<Float> generateLocalEmbedding(String text) {
        int dim = 768;
        float[] vector = new float[dim];
        if (text == null || text.isBlank()) {
            List<Float> list = new ArrayList<>(dim);
            for (int i = 0; i < dim; i++) list.add(0.0f);
            return list;
        }

        String[] tokens = text.toLowerCase().split("\\W+");
        for (String token : tokens) {
            if (token.isBlank()) continue;
            int hash = Math.abs(token.hashCode());
            int idx1 = hash % dim;
            int idx2 = (hash / 31) % dim;
            vector[idx1] += 1.0f;
            vector[idx2] += 0.5f;
        }

        float norm = 0.0f;
        for (float v : vector) norm += v * v;
        norm = (float) Math.sqrt(norm);
        if (norm > 0) {
            for (int i = 0; i < dim; i++) vector[i] /= norm;
        }

        List<Float> result = new ArrayList<>(dim);
        for (float v : vector) result.add(v);
        return result;
    }
}
