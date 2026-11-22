package com.Doctalk.Doctalk_backend.security.api;

import com.Doctalk.Doctalk_backend.security.dto.AuthentificationResponse;
import com.Doctalk.Doctalk_backend.security.dto.ChangePasswordDTO;
import com.Doctalk.Doctalk_backend.security.dto.LoginDTO;
import com.Doctalk.Doctalk_backend.security.dto.RegisterDTO;
import com.Doctalk.Doctalk_backend.security.dto.UserDTO;
import com.Doctalk.Doctalk_backend.security.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthentificationResponse> login(@RequestBody LoginDTO loginDTO) {
        AuthentificationResponse response = userService.authenticate(loginDTO);        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterDTO registerDTO) {
        UserDTO userDTO = userService.register(registerDTO);
        return new ResponseEntity<>(userDTO, HttpStatus.CREATED);
    }

    @GetMapping("/users")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/users/{publicId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<UserDTO> getUserByPublicId(@PathVariable UUID publicId) {
        return ResponseEntity.ok(userService.getUserByPublicId(publicId));
    }

    @PutMapping("/users/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID publicId,
                                              @RequestBody UserDTO userDTO) {
        return ResponseEntity.ok(userService.updateUser(publicId, userDTO));
    }

    @DeleteMapping("/users/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID publicId) {
        userService.deleteUser(publicId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{publicId}/enable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> enableUser(@PathVariable UUID publicId) {
        userService.enableUser(publicId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{publicId}/disable")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> disableUser(@PathVariable UUID publicId) {
        userService.disableUser(publicId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/users/{publicId}/change-password")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    public ResponseEntity<Void> changePassword(@PathVariable UUID publicId,
                                               @RequestBody ChangePasswordDTO changePasswordDTO) {
        userService.changePassword(publicId, changePasswordDTO);
        return ResponseEntity.noContent().build();
    }
}