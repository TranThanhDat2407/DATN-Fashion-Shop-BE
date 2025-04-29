package com.example.DATN_Fashion_Shop_BE.exception;

public class PermissionDenyException extends Exception{
    public PermissionDenyException(String message) {
        super(message);
    }
}