package com.example.easy_learning.exception;

public class EmailNotFoundException extends RuntimeException {
    public EmailNotFoundException(String message) { super(message); }
}