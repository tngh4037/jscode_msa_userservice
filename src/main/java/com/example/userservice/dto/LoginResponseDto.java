package com.example.userservice.dto;

// 로그인 요청시 응답 dto
public class LoginResponseDto {

    private String token;

    public LoginResponseDto(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }
}
