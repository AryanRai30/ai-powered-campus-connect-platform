package com.campusconnect.exception;

/**
 * Exception thrown when a StudentProfile already exists or conflict occurs.
 */
public class StudentProfileAlreadyExistsException extends RuntimeException {
    public StudentProfileAlreadyExistsException(String message) {
        super(message);
    }
}
