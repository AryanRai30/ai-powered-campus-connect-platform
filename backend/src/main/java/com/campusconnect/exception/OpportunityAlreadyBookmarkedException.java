package com.campusconnect.exception;

/**
 * Exception thrown when a student attempts to bookmark an opportunity they have already bookmarked.
 */
public class OpportunityAlreadyBookmarkedException extends RuntimeException {

    public OpportunityAlreadyBookmarkedException(String message) {
        super(message);
    }
}
