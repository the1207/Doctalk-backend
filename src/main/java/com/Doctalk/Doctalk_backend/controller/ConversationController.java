package com.Doctalk.Doctalk_backend.controller;

import com.Doctalk.Doctalk_backend.dto.reponse.ConversationDetailResponse;
import com.Doctalk.Doctalk_backend.dto.reponse.ConversationResponse;
import com.Doctalk.Doctalk_backend.dto.reponse.MessageResponse;
import com.Doctalk.Doctalk_backend.dto.reponse.Source;
import com.Doctalk.Doctalk_backend.dto.request.RenameConversationRequest;
import com.Doctalk.Doctalk_backend.entities.Conversation;
import com.Doctalk.Doctalk_backend.entities.Message;
import com.Doctalk.Doctalk_backend.repository.ConversationRepository;
import com.Doctalk.Doctalk_backend.repository.MessageRepository;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chat/conversations")
public class ConversationController {

    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;
    private final Gson gson = new Gson();

    public ConversationController(ConversationRepository conversationRepository,
                                  MessageRepository messageRepository) {
        this.conversationRepository = conversationRepository;
        this.messageRepository = messageRepository;
    }

    @GetMapping
    public ResponseEntity<List<ConversationResponse>> getAll() {
        List<ConversationResponse> responses = conversationRepository.findAll()
                .stream()
                .map(c -> new ConversationResponse(
                        c.getId(),
                        c.getTitle(),
                        c.getCreatedAt(),
                        c.getUpdatedAt(),
                        c.getMessages() != null ? c.getMessages().size() : 0
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ConversationDetailResponse> getById(@PathVariable Long id) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée avec l'id : " + id));

        List<Message> messages = messageRepository.findByConversationIdOrderByCreatedAtAsc(id);

        List<MessageResponse> messageResponses = messages.stream()
                .map(this::toMessageResponse)
                .collect(Collectors.toList());

        ConversationDetailResponse response = new ConversationDetailResponse(
                conversation.getId(),
                conversation.getTitle(),
                conversation.getCreatedAt(),
                conversation.getUpdatedAt(),
                messageResponses
        );
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ConversationResponse> rename(@PathVariable Long id,
                                                       @RequestBody RenameConversationRequest request) {
        Conversation conversation = conversationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Conversation non trouvée avec l'id : " + id));

        conversation.setTitle(request.title());
        Conversation updated = conversationRepository.save(conversation);

        ConversationResponse response = new ConversationResponse(
                updated.getId(),
                updated.getTitle(),
                updated.getCreatedAt(),
                updated.getUpdatedAt(),
                updated.getMessages() != null ? updated.getMessages().size() : 0
        );
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!conversationRepository.existsById(id)) {
            throw new RuntimeException("Conversation non trouvée avec l'id : " + id);
        }
        conversationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private MessageResponse toMessageResponse(Message message) {
        List<Source> sources = Collections.emptyList();
        if (message.getSources() != null && !message.getSources().isBlank()) {
            try {
                Type listType = new TypeToken<List<Source>>() {}.getType();
                sources = gson.fromJson(message.getSources(), listType);
            } catch (Exception e) {
                // sources mal formées (anciennes données) : on ignore silencieusement
                sources = Collections.emptyList();
            }
        }

        return new MessageResponse(
                message.getId(),
                message.getConversation().getId(),
                message.getRole(),
                message.getContent(),
                sources,
                message.getCreatedAt()
        );
    }
}