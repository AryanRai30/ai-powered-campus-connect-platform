package com.campusconnect.ai.exception;

/**
 * Custom exception thrown when Gemini API returns 503 Service Unavailable or transient service errors after retries.
 */
public class GeminiServiceUnavailableException extends RuntimeException {

    public GeminiServiceUnavailableException(String message) {
        super(message);
    }

    public GeminiServiceUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
