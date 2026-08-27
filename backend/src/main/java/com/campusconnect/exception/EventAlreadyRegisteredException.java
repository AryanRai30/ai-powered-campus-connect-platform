package com.campusconnect.exception;

/**
 * Exception thrown when a student attempts to register for an event they are already registered for.
 */
public class EventAlreadyRegisteredException extends RuntimeException {

    public EventAlreadyRegisteredException(String message) {
        super(message);
    }
}
