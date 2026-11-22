package com.Doctalk.Doctalk_backend.service;

import com.Doctalk.Doctalk_backend.dto.reponse.ChatResponse;
import com.Doctalk.Doctalk_backend.dto.request.ChatRequest;

public interface ChatService {
    ChatResponse ask(ChatRequest request);
}