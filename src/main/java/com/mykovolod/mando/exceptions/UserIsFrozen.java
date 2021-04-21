package com.mykovolod.mando.exceptions;

public class UserIsFrozen extends RuntimeException {
    public UserIsFrozen() {
        super("Too many request. User is frozen for some time");
    }
}
