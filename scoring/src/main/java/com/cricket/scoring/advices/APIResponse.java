package com.cricket.scoring.advices;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class APIResponse<T> {
    private LocalDateTime timeStamp;
    private T data;
    private APIError error;

    public APIResponse() {
        this.timeStamp = LocalDateTime.now();
    }
    public APIResponse(T data) {
        this();
        this.data = data;
    }
    public APIResponse(APIError error) {
        this();
        this.error = error;
    }
}
