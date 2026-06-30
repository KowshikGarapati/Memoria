package com.memoria.Memoria.exception;

public class UnauthorizedAccessException extends RuntimeException {

    public UnauthorizedAccessException() {
        super("You do not have permission to access this note.");
    }
}
