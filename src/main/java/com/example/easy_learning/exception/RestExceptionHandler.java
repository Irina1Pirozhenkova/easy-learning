package com.example.easy_learning.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
public class RestExceptionHandler {

  @ExceptionHandler(EmailNotFoundException.class)
  public ResponseEntity<?> handleEmailNotFound(EmailNotFoundException ex) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)   // 404
            .body(error(ex.getMessage()));
  }

  @ExceptionHandler(InvalidPasswordException.class)
  public ResponseEntity<?> handleInvalidPassword(InvalidPasswordException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
            .body(error(ex.getMessage()));
  }

  @ExceptionHandler(UserExistsException.class)
  public ResponseEntity<?> handleUserExists(UserExistsException ex) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED) // 401
            .body(error(ex.getMessage()));
  }

  private Map<String, Object> error(String msg) {
    return Map.of(
            "timestamp", LocalDateTime.now(),
            "message",   msg
    );
  }
}
