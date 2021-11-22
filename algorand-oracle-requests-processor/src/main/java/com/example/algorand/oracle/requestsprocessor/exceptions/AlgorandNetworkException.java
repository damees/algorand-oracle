package com.example.algorand.oracle.requestsprocessor.exceptions;

public class AlgorandNetworkException extends RuntimeException{

    public AlgorandNetworkException(String message) {
        super(message);
    }

    public AlgorandNetworkException(String message, Throwable t) {
        super(message, t);
    }
}
