package com.cricket.scoring.advices;

import com.cricket.scoring.exceptions.ResourceNotFoundException;
import com.cricket.scoring.exceptions.RuntimeConflictException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.naming.AuthenticationException;
import java.nio.file.AccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(RuntimeConflictException.class)
    public ResponseEntity<APIResponse<?>> handleRuntimeConflictException(RuntimeConflictException e) {
        APIError apiError = APIError.builder()
                .status(HttpStatus.CONFLICT)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<APIResponse<?>> handleResourceNotFoundException(ResourceNotFoundException e) {
        APIError apiError = APIError.builder()
                .status(HttpStatus.NOT_FOUND)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<APIResponse<?>> handleAuthenticationException(AuthenticationException e) {
        APIError apiError = APIError.builder()
                .status(HttpStatus.UNAUTHORIZED)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);

    }
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<APIResponse<?>> handleJwtException(AccessDeniedException e) {
        APIError apiError = APIError.builder()
                .status(HttpStatus.FORBIDDEN)
                .message(e.getMessage())
                .build();
        return buildErrorResponseEntity(apiError);
    }

    public ResponseEntity<APIResponse<?>> buildErrorResponseEntity(APIError apiError){
        return new ResponseEntity<>(new APIResponse<>(apiError), apiError.getStatus());
    }
}
