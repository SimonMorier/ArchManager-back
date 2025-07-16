package com.archmanager_back.service;

import com.archmanager_back.exception.InvalidCredentialsException;
import com.archmanager_back.exception.UsernameAlreadyTakenException;
import com.archmanager_back.mapper.UserMapper;
import com.archmanager_back.model.dto.AuthResponseDTO;
import com.archmanager_back.model.dto.UserRequestDTO;
import com.archmanager_back.model.dto.UserResponseDTO;
import com.archmanager_back.model.entity.jpa.User;
import com.archmanager_back.repository.jpa.UserRepository;
import com.archmanager_back.security.JwtUtil;
import com.archmanager_back.validator.UserValidator;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthResponseDTO register(UserRequestDTO request) {

        UserValidator.validateRegistration(request);

        userRepository.findByUsername(request.getUsername())
                .ifPresent(u -> {
                    throw new UsernameAlreadyTakenException(request.getUsername());
                });

        User toSave = userMapper.toEntity(request);
        toSave.setPassword(passwordEncoder.encode(request.getPassword()));

        User saved = userRepository.saveAndFlush(toSave);

        UserResponseDTO userDto = userMapper.toResponseDto(saved);
        String token = jwtUtil.generateToken(saved.getUsername());
        return new AuthResponseDTO(userDto, token);
    }

    public AuthResponseDTO login(String username, String password) {

        UserValidator.validateLogin(username, password);

        User user = userRepository.findByUsername(username)
                .orElseThrow(InvalidCredentialsException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException();
        }

        UserResponseDTO userDto = userMapper.toResponseDto(user);
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponseDTO(userDto, token);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
