package com.Doctalk.Doctalk_backend.service.implementation;

import com.Doctalk.Doctalk_backend.dto.reponse.ChatResponse;
import com.Doctalk.Doctalk_backend.dto.reponse.Source;
import com.Doctalk.Doctalk_backend.dto.request.ChatRequest;
import com.Doctalk.Doctalk_backend.entities.Chunk;
import com.Doctalk.Doctalk_backend.entities.Conversation;
import com.Doctalk.Doctalk_backend.entities.Message;
import com.Doctalk.Doctalk_backend.repository.ChunkRepository;
import com.Doctalk.Doctalk_backend.repository.ConversationRepository;
import com.Doctalk.Doctalk_backend.repository.MessageRepository;
import com.Doctalk.Doctalk_backend.service.ChatService;
import com.Doctalk.Doctalk_backend.service.EmbeddingService;
import com.google.genai.Client;
import com.google.genai.types.GenerateContentResponse;
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

    @Value("${gemini.api.key}")
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
        // 1. Récupérer ou créer la conversation
        Conversation conversation = getOrCreateConversation(request.conversationId());

        // 2. Sauvegarder la question de l'utilisateur
        Message userMessage = new Message("USER", request.question(), conversation);
        messageRepository.save(userMessage);

        // 3. Chercher les chunks pertinents
        List<Float> queryEmbedding = embeddingService.generateEmbedding(request.question());
        String vectorString = queryEmbedding.toString();
        List<Chunk> relevantChunks = chunkRepository.findSimilarChunksAll(vectorString, MAX_CHUNKS);

        // 4. Construire le prompt pour Gemini
        String prompt = buildPrompt(request.question(), relevantChunks);

        // 5. Appeler Gemini pour générer la réponse
        String answer = callGemini(prompt);

        // 6. Sauvegarder la réponse de l'assistant
        Message assistantMessage = new Message("ASSISTANT", answer, conversation);

        // 7. Ajouter les sources
        List<Source> sources = buildSources(relevantChunks);
        assistantMessage.setSources(sources.toString());
        messageRepository.save(assistantMessage);

        // 8. Retourner la réponse
        return new ChatResponse(
                conversation.getId(),
                answer,
                sources,
                LocalDateTime.now()
        );
    }

    private Conversation getOrCreateConversation(Long conversationId) {
        if (conversationId != null) {
            return conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new RuntimeException("Conversation non trouvée"));
        }
        Conversation newConversation = new Conversation("Nouvelle conversation");
        return conversationRepository.save(newConversation);
    }

    private String buildPrompt(String question, List<Chunk> chunks) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            context.append("Extrait ").append(i + 1).append(":\n")
                    .append(chunks.get(i).getContent())
                    .append("\n\n");
        }

        return "Tu es un assistant expert. Réponds à la question en te basant UNIQUEMENT sur les extraits suivants. "
                + "Si la réponse n'est pas dans les extraits, dis-le clairement. Ne invente pas d'informations.\n\n"
                + "### EXTRAITS :\n" + context.toString()
                + "### QUESTION :\n" + question + "\n\n"
                + "### RÉPONSE :";
    }

    private String callGemini(String prompt) {
        try {
            Client client = Client.builder()
                    .apiKey(apiKey)
                    .build();

            GenerateContentResponse response = client.models.generateContent(
                    "gemini-2.0-flash-exp",
                    prompt,
                    null
            );

            return response.text();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de l'appel à Gemini", e);
        }
    }

    private List<Source> buildSources(List<Chunk> chunks) {
        List<Source> sources = new ArrayList<>();
        for (Chunk chunk : chunks) {
            sources.add(new Source(
                    chunk.getId(),
                    chunk.getContent(),
                    chunk.getDocument().getTitle(),
                    0.9 // Pour l'instant, on met une valeur fixe
            ));
        }
        return sources;
    }
}