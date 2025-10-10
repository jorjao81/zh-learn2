package com.zhlearn.infrastructure.tencent;

/** Unchecked exception thrown when Tencent TTS client encounters an error. */
final class TencentTtsClientException extends RuntimeException {

    TencentTtsClientException(String message) {
        super(message);
    }

    TencentTtsClientException(String message, Throwable cause) {
        super(message, cause);
    }
}
