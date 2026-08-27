package com.campusconnect.exception;

/**
 * Exception thrown when a student attempts to join a club they have already joined.
 */
public class ClubAlreadyJoinedException extends RuntimeException {

    public ClubAlreadyJoinedException(String message) {
        super(message);
    }
}
