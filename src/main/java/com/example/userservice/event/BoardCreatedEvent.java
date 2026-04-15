package com.example.userservice.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class BoardCreatedEvent {
    private Long userId;

    public BoardCreatedEvent() {
    }

    public static BoardCreatedEvent fromJson(String message) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(message, BoardCreatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("JSON 파싱 실패", e);
        }
    }

    public Long getUserId() {
        return userId;
    }
}
