package com.example.userservice.dto;

// 로그인 요청 dto
public class LoginRequestDto {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
