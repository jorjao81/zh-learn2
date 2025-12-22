package com.zhlearn.infrastructure.minimax;

import com.zhlearn.domain.exception.UnrecoverableProviderException;

/**
 * Exception thrown when MiniMax TTS rejects content or encounters an unrecoverable error. This
 * represents an error that should be handled gracefully by skipping the problematic content rather
 * than crashing the application.
 */
public class MiniMaxContentException extends UnrecoverableProviderException {
    public MiniMaxContentException(String message) {
        super(message);
    }
}
