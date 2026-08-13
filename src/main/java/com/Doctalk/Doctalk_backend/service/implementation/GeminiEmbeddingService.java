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

    @Value("${gemini.api.key}")
    private String apiKey;

    private static final String GEMINI_EMBEDDING_URL = "https://generativelanguage.googleapis.com/v1beta/models/embedding-001:embedContent?key=";

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public List<Float> generateEmbedding(String text) {
        try {
            String url = GEMINI_EMBEDDING_URL + apiKey;

            // Construire le corps de la requête
            JsonObject requestBody = new JsonObject();
            JsonObject content = new JsonObject();

            // IMPORTANT : "parts" doit être un JsonArray, pas un JsonObject
            JsonArray partsArray = new JsonArray();
            JsonObject part = new JsonObject();
            part.addProperty("text", text);
            partsArray.add(part);

            content.add("parts", partsArray);
            requestBody.add("content", content);

            // Créer la requête HTTP
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                    .build();

            // Envoyer la requête
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            // Vérifier la réponse
            if (response.statusCode() != 200) {
                throw new RuntimeException("Erreur Gemini API: " + response.statusCode() + " - " + response.body());
            }

            // Extraire l'embedding de la réponse
            JsonObject jsonResponse = JsonParser.parseString(response.body()).getAsJsonObject();
            JsonArray embeddingArray = jsonResponse.getAsJsonObject("embedding")
                    .getAsJsonArray("values");

            List<Float> embedding = new ArrayList<>();
            for (int i = 0; i < embeddingArray.size(); i++) {
                embedding.add(embeddingArray.get(i).getAsFloat());
            }

            return embedding;

        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération de l'embedding avec Gemini", e);
        }
    }
}