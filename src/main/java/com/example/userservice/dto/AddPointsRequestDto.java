package com.example.userservice.dto;

// pointservice 에 포인트적립 요청 할 dto
public class AddPointsRequestDto {
    private Long userId;
    private int amount;

    public AddPointsRequestDto(Long userId, int amount) {
        this.userId = userId;
        this.amount = amount;
    }

    public Long getUserId() {
        return userId;
    }

    public int getAmount() {
        return amount;
    }
}
