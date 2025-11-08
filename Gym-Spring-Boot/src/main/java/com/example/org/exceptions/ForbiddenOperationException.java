package com.example.org.exceptions;

public class ForbiddenOperationException extends Exception {

    public ForbiddenOperationException(String message){
        super(message);
    }

}
