package ru.t1.account_processing.exception;

public class AccountNotExistForClientException extends RuntimeException {
    public AccountNotExistForClientException(String message) {
        super(message);
    }
}
