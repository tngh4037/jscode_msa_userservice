package com.example.userservice.service;

import com.example.userservice.domain.User;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.domain.UserRepository;
import com.example.userservice.dto.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public void signUp(SignUpRequestDto signUpRequestDto) {
        User user = new User(
                signUpRequestDto.getEmail(),
                signUpRequestDto.getName(),
                signUpRequestDto.getPassword()
        );

        this.userRepository.save(user);
    }

    public UserResponseDto getUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        return new UserResponseDto(
                user.getUserId(),
                user.getEmail(),
                user.getName());
    }

    public List<UserResponseDto> getUsersByIds(List<Long> ids) {
        List<User> users = userRepository.findAllById(ids);

        return users.stream().map(user -> new UserResponseDto(user.getUserId(), user.getEmail(), user.getName()))
                .collect(Collectors.toList());
    }
}
