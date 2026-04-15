package com.example.userservice.service;

import com.example.userservice.client.PointClient;
import com.example.userservice.domain.User;
import com.example.userservice.dto.AddActivityScoreRequestDto;
import com.example.userservice.dto.SignUpRequestDto;
import com.example.userservice.domain.UserRepository;
import com.example.userservice.dto.UserResponseDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final int SIGN_UP_POINT_AMOUNT = 1_000;

    private final UserRepository userRepository;
    private final PointClient pointClient;

    public UserService(UserRepository userRepository, PointClient pointClient) {
        this.userRepository = userRepository;
        this.pointClient = pointClient;
    }

    @Transactional
    public void signUp(SignUpRequestDto signUpRequestDto) {
        User user = new User(
                signUpRequestDto.getEmail(),
                signUpRequestDto.getName(),
                signUpRequestDto.getPassword()
        );

        User savedUser = this.userRepository.save(user); // 회원가입
        this.pointClient.addPoints(savedUser.getUserId(), SIGN_UP_POINT_AMOUNT); // 포인트 적립
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

    // 활동 점수 적립
    @Transactional
    public void addActivityScore(AddActivityScoreRequestDto addActivityScoreRequestDto) {
        User user = userRepository.findById(addActivityScoreRequestDto.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        user.addActivityScore(addActivityScoreRequestDto.getScore());

        userRepository.save(user);

        // 의도적 에러 발생 코드
        // throw new RuntimeException("에러 발생");
    }
}
