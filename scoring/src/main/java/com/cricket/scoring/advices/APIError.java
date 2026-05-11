package com.cricket.scoring.advices;

import lombok.Builder;
import lombok.Data;
import org.springframework.http.HttpStatus;

import java.util.List;

@Data
@Builder
public class APIError {
    private String message;
    private HttpStatus status;
    private List<String> subErrors;
}
