package com.Doctalk.Doctalk_backend.Exeption;

public class FolderNotFoundException extends ResourceNotFoundException {
    public FolderNotFoundException(Long id) {
        super("Dossier non trouvé avec l'id : " + id);
    }
}
