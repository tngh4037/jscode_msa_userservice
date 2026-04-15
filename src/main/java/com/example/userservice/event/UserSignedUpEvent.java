package com.example.userservice.event;

// kafka 로 전달할 메시지 객체 ( for 데이터 동기화 )
public class UserSignedUpEvent {
    private Long userId;
    private String name;

    public UserSignedUpEvent(Long userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public Long getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
