package com.campusconnect.exception;

/**
 * Custom runtime exception thrown when attempting to register a user with an already registered email.
 */
public class UserAlreadyExistsException extends RuntimeException {

    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
