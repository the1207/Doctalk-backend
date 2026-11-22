package com.Doctalk.Doctalk_backend.security.service;

import com.Doctalk.Doctalk_backend.security.dto.AuthentificationResponse;
import com.Doctalk.Doctalk_backend.security.dto.ChangePasswordDTO;
import com.Doctalk.Doctalk_backend.security.dto.LoginDTO;
import com.Doctalk.Doctalk_backend.security.dto.RegisterDTO;
import com.Doctalk.Doctalk_backend.security.dto.UserDTO;
import com.Doctalk.Doctalk_backend.Exeption.AccountDisabledException;
import com.Doctalk.Doctalk_backend.Exeption.InvalidCredentialsException;
import com.Doctalk.Doctalk_backend.Exeption.ResourceNotFoundException;
import com.Doctalk.Doctalk_backend.Exeption.AlreadyExistException;
import com.Doctalk.Doctalk_backend.security.jwt.JwtUtils;
import com.Doctalk.Doctalk_backend.security.model.User;
import com.Doctalk.Doctalk_backend.security.repository.UserRepository;
import com.Doctalk.Doctalk_backend.security.service.UserService;
import com.Doctalk.Doctalk_backend.security.UserDetailsImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;

    public UserServiceImpl(UserRepository userRepository,
                           PasswordEncoder passwordEncoder,
                           AuthenticationManager authenticationManager,
                           JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public AuthentificationResponse authenticate(LoginDTO loginDTO) {
        User user = userRepository.findByUsername(loginDTO.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!user.isEnable()) {
            throw new AccountDisabledException("Votre compte est désactivé");
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginDTO.getUsername(),
                            loginDTO.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String token = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return new AuthentificationResponse(
                    token,
                    userDetails.getPublicId(),
                    userDetails.getUsername(),
                    userDetails.getRole()
            );

        } catch (BadCredentialsException ex) {
            throw new InvalidCredentialsException("Nom d'utilisateur ou mot de passe incorrect");
        }
    }

    @Override
    public UserDTO register(RegisterDTO registerDTO) {
        if (userRepository.existsByUsername(registerDTO.getUsername())) {
            throw new AlreadyExistException("Ce nom d'utilisateur existe déjà");
        }

        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRole(registerDTO.getRole() != null ? registerDTO.getRole() : "USER");
        user.setEnable(true);

        User savedUser = userRepository.save(user);

        return mapToUserDTO(savedUser);
    }

    @Override
    public List<UserDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserDTO)
                .collect(Collectors.toList());
    }

    @Override
    public UserDTO getUserByPublicId(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        return mapToUserDTO(user);
    }

    @Override
    public UserDTO updateUser(UUID publicId, UserDTO userDTO) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        user.setUsername(userDTO.getUsername());
        user.setRole(userDTO.getRole());
        user.setEnable(userDTO.isEnable());

        User updatedUser = userRepository.save(user);
        return mapToUserDTO(updatedUser);
    }

    @Override
    public void deleteUser(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        userRepository.delete(user);
    }

    @Override
    public void enableUser(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setEnable(true);
        userRepository.save(user);
    }

    @Override
    public void disableUser(UUID publicId) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));
        user.setEnable(false);
        userRepository.save(user);
    }

    @Override
    public void changePassword(UUID publicId, ChangePasswordDTO changePasswordDTO) {
        User user = userRepository.findByPublicId(publicId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur non trouvé"));

        if (!passwordEncoder.matches(changePasswordDTO.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Mot de passe actuel incorrect");
        }

        user.setPassword(passwordEncoder.encode(changePasswordDTO.getNewPassword()));
        userRepository.save(user);
    }

    private UserDTO mapToUserDTO(User user) {
        return new UserDTO(
                user.getPublicId(),
                user.getUsername(),
                user.getRole(),
                user.isEnable(),
                user.getCreatedAt()
        );
    }

}