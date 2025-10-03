package ru.t1.credit_processing.exception;

public class ProductRegistryNotFoundException extends RuntimeException {
    public ProductRegistryNotFoundException(String message) {
        super(message);
    }

}
