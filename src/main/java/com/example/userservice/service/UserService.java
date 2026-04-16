package com.example.userservice.service;

import com.example.userservice.client.PointClient;
import com.example.userservice.domain.User;
import com.example.userservice.dto.*;
import com.example.userservice.domain.UserRepository;
import com.example.userservice.event.UserSignedUpEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final int SIGN_UP_POINT_AMOUNT = 1_000;

    private final UserRepository userRepository;
    private final PointClient pointClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final String jwtSecret;

    public UserService(UserRepository userRepository,
                       PointClient pointClient,
                       KafkaTemplate<String, String> kafkaTemplate,
                       @Value("${jwt.secret}") String jwtSecret) {
        this.userRepository = userRepository;
        this.pointClient = pointClient;
        this.kafkaTemplate = kafkaTemplate;
        this.jwtSecret = jwtSecret;
    }

    @Transactional
    public void signUp(SignUpRequestDto signUpRequestDto) {
        User user = new User(
                signUpRequestDto.getEmail(),
                signUpRequestDto.getName(),
                signUpRequestDto.getPassword()
        );

        // 회원가입
        User savedUser = this.userRepository.save(user);

        // 포인트 적립
        this.pointClient.addPoints(savedUser.getUserId(), SIGN_UP_POINT_AMOUNT);

        // 회원가입 완료 이벤트 발행
        UserSignedUpEvent userSignedUpEvent = new UserSignedUpEvent(
                savedUser.getUserId(),
                savedUser.getName()
        );
        this.kafkaTemplate.send("user.signed-up", toJsonString(userSignedUpEvent));
    }
    
    private String toJsonString(Object object) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 직렬화 실패", e);
        }
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

    // 로그인 처리 ( 참고. 스크링 시큐리티를 사용하지 않고 직접 구현 )
    public LoginResponseDto login(LoginRequestDto loginRequestDto) {
        User user = userRepository.findByEmail(loginRequestDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

        if (!user.getPassword().equals(loginRequestDto.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        // [ JWT 로직 ]
        // jwt 토큰을 생성할 때 사용하는 Key 생성 (공식 문서 방식)
        SecretKey secretKey = Keys.hmacShaKeyFor(
                jwtSecret.getBytes(StandardCharsets.UTF_8)
        );

        // jwt 토큰 생성
        String token = Jwts.builder()
                .subject(user.getUserId().toString()) // userId 값을 담아서 토큰 생성 (문자만 가능)
                .signWith(secretKey)
                .compact();

        return new LoginResponseDto(token);
    }
}
