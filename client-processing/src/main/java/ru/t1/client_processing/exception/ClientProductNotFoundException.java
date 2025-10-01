package ru.t1.client_processing.exception;

public class ClientProductNotFoundException extends RuntimeException {
    public ClientProductNotFoundException(String message) {
        super(message);
    }
}
