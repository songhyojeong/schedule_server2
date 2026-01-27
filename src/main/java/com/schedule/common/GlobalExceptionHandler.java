package com.schedule.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ResponseStatusException;

import com.schedule.user.dto.ResponseDTO;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // ResponseStatusException 처리
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ResponseDTO<Void>> handleResponseStatus(ResponseStatusException e) {
        return ResponseEntity
                .status(e.getStatusCode())
                .body(ResponseDTO.setFailed(e.getReason()));
    }

    // IllegalArgumentException 처리
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseDTO<Void>> handleIllegalArgument(IllegalArgumentException e) {
        return ResponseEntity
                .badRequest()
                .body(ResponseDTO.setFailed(e.getMessage()));
    }

    // IllegalStateException 처리
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ResponseDTO<Void>> handleIllegalState(IllegalStateException e) {
        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(ResponseDTO.setFailed(e.getMessage()));
    }

    // 일반 Exception 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseDTO<Void>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ResponseDTO.setFailed("처리 중 오류가 발생했습니다: " + e.getMessage()));
    }
}