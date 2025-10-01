package ru.t1.client_processing.exception;

public class BlacklistedClientException extends RuntimeException{
    public BlacklistedClientException(String message) {
        super(message);
    }
}
