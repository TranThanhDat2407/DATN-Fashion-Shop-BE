package com.example.DATN_Fashion_Shop_BE.exception;

public class ExpiredTokenException extends Exception{
    public ExpiredTokenException(String message){
        super(message);
    }
}
