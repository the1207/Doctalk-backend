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
        Conversation conversation = getOrCreateConversation(request.conversationId());

        Message userMessage = new Message("USER", request.question(), conversation);
        messageRepository.save(userMessage);

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
            answer = "Je n'ai trouvé aucun contenu pertinent dans les documents vectorisés. Merci de téléverser un document ou de vérifier qu'il est bien traité.";
        } else {
            String prompt = buildPrompt(request.question(), relevantChunks, request.documentId() != null);
            answer = callGemini(prompt);
        }

        Message assistantMessage = new Message("ASSISTANT", answer, conversation);

        List<Source> sources = buildSources(relevantChunks);
        // Sérialisation en JSON réel (au lieu de List.toString())
        assistantMessage.setSources(gson.toJson(sources));
        messageRepository.save(assistantMessage);

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

    private String buildPrompt(String question, List<ChunkSimilarityProjection> chunks, boolean isDocumentContext) {
        StringBuilder context = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            context.append("Extrait ").append(i + 1).append(":\n")
                    .append(chunks.get(i).getContent())
                    .append("\n\n");
        }

        String baseInstruction = "Tu es un assistant expert.";
        if (isDocumentContext) {
            baseInstruction += " Réponds en priorité à partir du document sélectionné.";
        } else {
            baseInstruction += " Réponds uniquement à partir des documents disponibles.";
        }

        return baseInstruction + " Si la réponse n'est pas dans les extraits, dis-le clairement. Ne invente pas d'informations.\n\n"
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