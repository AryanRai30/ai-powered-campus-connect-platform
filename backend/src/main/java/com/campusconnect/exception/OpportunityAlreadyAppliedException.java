package com.campusconnect.exception;

/**
 * Exception thrown when a student attempts to mark an opportunity as applied when already tracked.
 */
public class OpportunityAlreadyAppliedException extends RuntimeException {

    public OpportunityAlreadyAppliedException(String message) {
        super(message);
    }
}
