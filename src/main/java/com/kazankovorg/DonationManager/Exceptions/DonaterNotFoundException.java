package com.kazankovorg.DonationManager.Exceptions;

public class DonaterNotFoundException extends RuntimeException{
    public DonaterNotFoundException(String message) {
        super(message);
    }
}
