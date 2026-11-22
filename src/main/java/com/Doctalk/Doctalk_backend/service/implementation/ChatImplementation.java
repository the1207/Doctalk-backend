package com.Doctalk.Doctalk_backend.service.implementation;

import com.Doctalk.Doctalk_backend.dto.reponse.ChatResponse;
import com.Doctalk.Doctalk_backend.dto.reponse.Source;
import com.Doctalk.Doctalk_backend.dto.request.ChatRequest;
import com.Doctalk.Doctalk_backend.repository.ChunkSimilarityProjection;
import com.Doctalk.Doctalk_backend.entities.Conversation;
import com.Doctalk.Doctalk_backend.entities.Message;
import com.Doctalk.Doctalk_backend.repository.ChunkRepository;
import com.Doctalk.Doctalk_backend.utils.VectorUtils;
import com.Doctalk.Doctalk_backend.repository.ConversationRepository;
import com.Doctalk.Doctalk_backend.repository.MessageRepository;
import com.Doctalk.Doctalk_backend.service.ChatService;
import com.Doctalk.Doctalk_backend.service.EmbeddingService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class ChatImplementation implements ChatService {

    private final EmbeddingService embeddingService;
    private final ChunkRepository chunkRepository;
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final Gson gson = new Gson();

    @Value("${gemini.api.key:}")
    private String apiKey;

    private static final int MAX_CHUNKS = 5;

    public ChatImplementation(EmbeddingService embeddingService,
                              ChunkRepository chunkRepository,
                              ConversationRepository conversationRepository,
                              MessageRepository messageRepository) {
        this.embeddingService = embeddingService;
        this.chunkRepository = chunkRepository;
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional
    public ChatResponse ask(ChatRequest request) {
        System.out.println("[DOCDEBUG] ChatImplementation v2 called with question: " + request.question());
        Conversation conversation = getOrCreateConversation(request.conversationId());

        Message userMessage = new Message("USER", request.question(), conversation);
        messageRepository.save(userMessage);

        // Détecter si c'est un message conversationnel (salut, bonjour, merci...)
        String question = request.question().trim().toLowerCase();
        if (isConversational(question)) {
            String greetingAnswer = respondConversationally(request.question());
            Message assistantMessage = new Message("ASSISTANT", greetingAnswer, conversation);
            assistantMessage.setSources(gson.toJson(new ArrayList<Source>()));
            messageRepository.save(assistantMessage);
            return new ChatResponse(conversation.getId(), greetingAnswer, new ArrayList<>(), LocalDateTime.now());
        }

        List<Float> queryEmbedding = embeddingService.generateEmbedding(request.question());
        String vectorString = VectorUtils.toPgVectorLiteral(queryEmbedding);

        List<ChunkSimilarityProjection> relevantChunks;
        if (request.documentId() != null) {
            relevantChunks = chunkRepository.findSimilarChunksWithScore(request.documentId(), vectorString, MAX_CHUNKS);
        } else {
            relevantChunks = chunkRepository.findSimilarChunksAllWithScore(vectorString, MAX_CHUNKS);
        }

        String answer;
        if (relevantChunks.isEmpty()) {
            answer = "Je n'ai trouvé aucun contenu pertinent dans les documents pour cette question. "
                   + "Téléversez un document ou reformulez votre question.";
        } else {
            String prompt = buildPrompt(request.question(), relevantChunks, request.documentId() != null);
            answer = callGeminiOrFallback(prompt, relevantChunks);
        }

        Message assistantMessage = new Message("ASSISTANT", answer, conversation);

        List<Source> sources = buildSources(relevantChunks);
        assistantMessage.setSources(gson.toJson(sources));
        messageRepository.save(assistantMessage);

        return new ChatResponse(
                conversation.getId(),
                answer,
                sources,
                LocalDateTime.now()
        );
    }

    private boolean isConversational(String question) {
        String[] greetings = {"salut", "bonjour", "bonsoir", "hello", "hi", "coucou", "hey",
                              "merci", "d'accord", "ok", "super", "génial", "parfait",
                              "au revoir", "bye", "comment ça va", "ça va"};
        for (String greeting : greetings) {
            if (question.equals(greeting) || question.startsWith(greeting + " ") || question.startsWith(greeting + "!")) {
                return true;
            }
        }
        return question.length() < 10 && !question.contains("?") && !question.contains("qu");
    }

    private String respondConversationally(String originalQuestion) {
        String q = originalQuestion.trim().toLowerCase();
        if (q.contains("salut") || q.contains("bonjour") || q.contains("hello") || q.contains("hi") || q.contains("coucou") || q.contains("hey")) {
            return "Salut ! 👋 Je suis DocTalk, ton assistant de cours. Pose-moi une question sur ton document "
                 + "et je te répondrai en m'appuyant sur son contenu. Tu peux aussi aller dans l'Espace Révision "
                 + "pour générer des quiz, des flashcards ou des fiches de révision.";
        }
        if (q.contains("merci")) {
            return "De rien ! 🙌 N'hésite pas si tu as d'autres questions sur ton document.";
        }
        if (q.contains("ça va") || q.contains("comment ça va")) {
            return "Je vais très bien, merci ! Je suis prêt à t'aider avec tes documents. Pose-moi une question !";
        }
        if (q.contains("au revoir") || q.contains("bye")) {
            return "À bientôt ! 👋";
        }
        return "Je suis là pour t'aider. Pose-moi une question sur ton document et je te répondrai à partir de son contenu.";
    }

    private Conversation getOrCreateConversation(Long conversationId) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        }
        Conversation newConversation = new Conversation("Nouvelle conversation");
        return conversationRepository.save(newConversation);
    }

    private String buildPrompt(String question, List<ChunkSimilarityProjection> chunks, boolean isDocumentContext) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            // Limiter chaque extrait à 800 caractères pour éviter de saturer le contexte
            String content = chunks.get(i).getContent();
            if (content.length() > 800) {
                content = content.substring(0, 800) + "...";
            }
            context.append("[Extrait ").append(i + 1).append("]\n")
                    .append(content)
                    .append("\n\n");
        }

        return "Tu es DocTalk, un assistant pédagogique qui aide les étudiants à comprendre leurs documents. "
                + "L'utilisateur t'a posé une question. Réponds de façon naturelle et conversationnelle.\n\n"
                + "Règles :\n"
                + "- Réponds à la question en t'appuyant sur les extraits ci-dessous.\n"
                + "- Sois clair, concis et structuré. Utilise des listes si pertinent.\n"
                + "- Si la réponse n'est pas dans les extraits, dis-le simplement.\n"
                + "- N'invente jamais d'informations.\n"
                + "- Ne recopie pas les extraits tels quels : reformule avec tes propres mots.\n\n"
                + "### EXTRAITS DU DOCUMENT :\n" + context.toString()
                + "### QUESTION DE L'ÉTUDIANT :\n" + question + "\n\n"
                + "### TA RÉPONSE :\n";
    }

    private String callGeminiOrFallback(String prompt, List<ChunkSimilarityProjection> chunks) {
        System.out.println("[DOCDEBUG] API key present: " + (apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("${")) + ", length: " + (apiKey != null ? apiKey.length() : 0) + ", starts with: " + (apiKey != null && apiKey.length() > 3 ? apiKey.substring(0, 3) : "none"));
        if (apiKey != null && !apiKey.isBlank() && !apiKey.startsWith("${")) {
            // Essayer avec le SDK Google Gen AI qui supporte nativement les clés AQ.
            try {
                com.google.genai.Client client = com.google.genai.Client.builder()
                        .apiKey(apiKey)
                        .build();

                com.google.genai.types.GenerateContentResponse response = client.models.generateContent(
                        "gemini-2.0-flash",
                        prompt,
                        null
                );

                String text = response.text();
                if (text != null && !text.isBlank()) {
                    System.out.println("[DOCDEBUG] Gemini SDK success! Response length: " + text.length());
                    return text;
                }
            } catch (Exception e) {
                System.out.println("[DOCDEBUG] Gemini SDK failed: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            }

            // Essayer avec x-goog-api-key header
            try {
                return callGeminiHttp(prompt);
            } catch (Exception e) {
                System.out.println("[DOCDEBUG] Gemini HTTP failed: " + e.getMessage());
            }
        }

        // Fallback : reformuler les extraits de façon concise
        StringBuilder sb = new StringBuilder();
        sb.append("D'après le contenu de votre document, voici ce que j'ai trouvé :\n\n");
        for (int i = 0; i < Math.min(chunks.size(), 3); i++) {
            String content = chunks.get(i).getContent();
            // Résumer : prendre les 200 premiers caractères significatifs
            if (content.length() > 200) {
                content = content.substring(0, 200) + "...";
            }
            sb.append("• ").append(content.trim()).append("\n\n");
        }
        sb.append("_Note : La génération complète nécessite une clé API Gemini valide configurée dans les variables d'environnement._");
        return sb.toString();
    }

    private String callGeminiHttp(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        com.google.gson.JsonObject requestBody = new com.google.gson.JsonObject();
        com.google.gson.JsonArray contents = new com.google.gson.JsonArray();
        com.google.gson.JsonObject content = new com.google.gson.JsonObject();
        com.google.gson.JsonArray parts = new com.google.gson.JsonArray();
        com.google.gson.JsonObject part = new com.google.gson.JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .header("x-goog-api-key", apiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini API error: " + response.statusCode() + " - " + response.body().substring(0, Math.min(300, response.body().length())));
        }

        com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
        com.google.gson.JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            com.google.gson.JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            com.google.gson.JsonObject contentObj = firstCandidate.getAsJsonObject("content");
            com.google.gson.JsonArray partsArr = contentObj.getAsJsonArray("parts");
            return partsArr.get(0).getAsJsonObject().get("text").getAsString();
        }

        throw new RuntimeException("Gemini API returned no content");
    }

    private String callGeminiHttpBearer(String prompt) throws Exception {
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";

        com.google.gson.JsonObject requestBody = new com.google.gson.JsonObject();
        com.google.gson.JsonArray contents = new com.google.gson.JsonArray();
        com.google.gson.JsonObject content = new com.google.gson.JsonObject();
        com.google.gson.JsonArray parts = new com.google.gson.JsonArray();
        com.google.gson.JsonObject part = new com.google.gson.JsonObject();
        part.addProperty("text", prompt);
        parts.add(part);
        content.add("parts", parts);
        contents.add(content);
        requestBody.add("contents", contents);

        java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                .uri(java.net.URI.create(url))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + apiKey)
                .POST(java.net.http.HttpRequest.BodyPublishers.ofString(requestBody.toString()))
                .build();

        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Gemini Bearer error: " + response.statusCode() + " - " + response.body().substring(0, Math.min(200, response.body().length())));
        }

        com.google.gson.JsonObject jsonResponse = com.google.gson.JsonParser.parseString(response.body()).getAsJsonObject();
        com.google.gson.JsonArray candidates = jsonResponse.getAsJsonArray("candidates");
        if (candidates != null && !candidates.isEmpty()) {
            com.google.gson.JsonObject firstCandidate = candidates.get(0).getAsJsonObject();
            com.google.gson.JsonObject contentObj = firstCandidate.getAsJsonObject("content");
            com.google.gson.JsonArray partsArr = contentObj.getAsJsonArray("parts");
            return partsArr.get(0).getAsJsonObject().get("text").getAsString();
        }

        throw new RuntimeException("Gemini Bearer API returned no content");
    }

    private List<Source> buildSources(List<ChunkSimilarityProjection> chunks) {
        List<Source> sources = new ArrayList<>();
        for (ChunkSimilarityProjection chunk : chunks) {
            sources.add(new Source(
                    chunk.getId(),
                    chunk.getContent(),
                    chunk.getDocumentTitle(),
                    chunk.getSimilarity()
            ));
        }
        return sources;
    }
}
