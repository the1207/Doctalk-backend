package com.Doctalk.Doctalk_backend.security.service;

import com.Doctalk.Doctalk_backend.security.dto.*;

import java.util.List;
import java.util.UUID;

public interface UserService {  // ← Supprime "extends UserDetailsService"

    AuthentificationResponse authenticate(LoginDTO loginDTO);

    UserDTO register(RegisterDTO registerDTO);

    List<UserDTO> getAllUsers();

    UserDTO getUserByPublicId(UUID publicId);

    UserDTO updateUser(UUID publicId, UserDTO userDTO);

    void deleteUser(UUID publicId);

    void enableUser(UUID publicId);

    void disableUser(UUID publicId);

    void changePassword(UUID publicId, ChangePasswordDTO changePasswordDTO);
}