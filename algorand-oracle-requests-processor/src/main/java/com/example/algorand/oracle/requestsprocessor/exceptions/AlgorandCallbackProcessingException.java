package com.example.algorand.oracle.requestsprocessor.exceptions;

public class AlgorandCallbackProcessingException extends RuntimeException{

    public AlgorandCallbackProcessingException(String message) {
        super(message);
    }

    public AlgorandCallbackProcessingException(String message, Throwable t) {
        super(message, t);
    }
}
