package com.Doctalk.Doctalk_backend.controller;

import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.Doctalk.Doctalk_backend.Exeption.ConversationNotFoundException;
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
                .orElseThrow(() -> new ConversationNotFoundException(id));

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
                .orElseThrow(() -> new ConversationNotFoundException(id));

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
            throw new ConversationNotFoundException(id);
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