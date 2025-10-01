package ru.t1.client_processing.exception;

public class ClientProductAlreadyExistsException extends RuntimeException {
    public ClientProductAlreadyExistsException(String message) {
        super(message);
    }
}