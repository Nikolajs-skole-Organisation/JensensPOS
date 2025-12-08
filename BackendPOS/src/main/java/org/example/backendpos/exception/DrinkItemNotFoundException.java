package org.example.backendpos.exception;

public class DrinkItemNotFoundException extends RuntimeException {
    public DrinkItemNotFoundException(String message) {
        super(message);
    }
}
