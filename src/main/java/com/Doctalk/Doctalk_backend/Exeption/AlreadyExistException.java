package com.Doctalk.Doctalk_backend.Exeption;

public class AlreadyExistException extends RuntimeException {

    public AlreadyExistException(String message){
        super(message);
    }
}
