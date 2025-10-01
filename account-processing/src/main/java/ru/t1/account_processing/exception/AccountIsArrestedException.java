package ru.t1.account_processing.exception;

public class AccountIsArrestedException extends RuntimeException {
    public AccountIsArrestedException(String message) {
        super(message);
    }
}
