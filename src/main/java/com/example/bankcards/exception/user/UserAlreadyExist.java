package com.example.bankcards.exception.user;

public class UserAlreadyExist extends RuntimeException{

    public UserAlreadyExist(String message){
        super(message);
    }

}
