package com.mykovolod.mando.exceptions;

public class UserAlreadyExist extends RuntimeException {
    public UserAlreadyExist() {
        super("User already exists");
    }
}
