package com.Doctalk.Doctalk_backend.controller;

import com.Doctalk.Doctalk_backend.dto.reponse.ChatResponse;
import com.Doctalk.Doctalk_backend.dto.request.ChatRequest;
import com.Doctalk.Doctalk_backend.service.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    public ChatController(ChatService chatService) {
        this.chatService = chatService;
    }

    @PostMapping
    public ResponseEntity<ChatResponse> ask(@RequestBody ChatRequest request) {
        ChatResponse response = chatService.ask(request);
        return ResponseEntity.ok(response);
    }
}