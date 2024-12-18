package com.samsa;

public class FlowLoadException extends RuntimeException {
    public FlowLoadException(String message) {
        super(message);
    }

    public FlowLoadException(String message, Throwable cause) {
        super(message, cause);
    }
}
