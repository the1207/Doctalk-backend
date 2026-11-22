package com.Doctalk.Doctalk_backend.Exeption;

public class DocumentNotFoundException extends ResourceNotFoundException {
    public DocumentNotFoundException(Long id) {
        super("Document non trouvé avec l'id : " + id);
    }
}
