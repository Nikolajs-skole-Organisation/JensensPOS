package org.example.backendpos.exception;

public class MissingMeatTemperatureException extends RuntimeException {
    public MissingMeatTemperatureException(String message) {
        super(message);
    }
}
