package com.mykovolod.mando.exceptions;

public class UserIsBlocked extends RuntimeException {
    public UserIsBlocked(){
        super("User is blocked");
    }
}
