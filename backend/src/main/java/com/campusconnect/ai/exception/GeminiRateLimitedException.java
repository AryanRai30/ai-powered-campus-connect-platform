package com.campusconnect.ai.exception;

/**
 * Custom exception thrown when Gemini API short-term per-minute rate limit (RATE_LIMITED / RPM 429) is reached.
 */
public class GeminiRateLimitedException extends RuntimeException {

    public GeminiRateLimitedException(String message) {
        super(message);
    }

    public GeminiRateLimitedException(String message, Throwable cause) {
        super(message, cause);
    }
}
