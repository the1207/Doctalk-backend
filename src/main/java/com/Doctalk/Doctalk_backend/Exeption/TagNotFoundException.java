package com.Doctalk.Doctalk_backend.Exeption;

public class TagNotFoundException extends ResourceNotFoundException {
    public TagNotFoundException(Long id) {
        super("Tag non trouvé avec l'id : " + id);
    }
}
