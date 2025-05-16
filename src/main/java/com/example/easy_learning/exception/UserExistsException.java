package com.example.easy_learning.exception;

public class UserExistsException extends RuntimeException {
  public UserExistsException(String message) { super(message); }
}
