package com.Doctalk.Doctalk_backend.Exeption;

public class ConversationNotFoundException extends ResourceNotFoundException {
    public ConversationNotFoundException(Long id) {
        super("Conversation non trouvée avec l'id : " + id);
    }
}
