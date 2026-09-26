package com.campusconnect.ai.exception;

/**
 * Custom exception thrown when Gemini API free-tier quota or rate limit (429 RESOURCE_EXHAUSTED) is reached.
 */
public class GeminiQuotaExceededException extends RuntimeException {

    public GeminiQuotaExceededException(String message) {
        super(message);
    }

    public GeminiQuotaExceededException(String message, Throwable cause) {
        super(message, cause);
    }
}
