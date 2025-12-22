package com.zhlearn.infrastructure.tencent;

import java.time.Duration;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;
import com.zhlearn.infrastructure.ratelimit.ProviderRateLimiter;

class TencentTtsClient {
    private static final Logger log = LoggerFactory.getLogger(TencentTtsClient.class);
    private static final Duration RATE_LIMIT_ACQUIRE_TIMEOUT = Duration.ofSeconds(30);

    private final TtsClient client;
    private final ProviderRateLimiter rateLimiter;

    TencentTtsClient(TtsClient client) {
        this(client, null);
    }

    TencentTtsClient(TtsClient client, ProviderRateLimiter rateLimiter) {
        this.client = client;
        this.rateLimiter = rateLimiter;
    }

    TencentTtsClient(
            String secretId, String secretKey, String region, ProviderRateLimiter rateLimiter) {
        if (secretId == null || secretId.isBlank()) {
            throw new IllegalArgumentException("Secret ID missing for Tencent Cloud request");
        }
        if (secretKey == null || secretKey.isBlank()) {
            throw new IllegalArgumentException("Secret key missing for Tencent Cloud request");
        }
        if (region == null || region.isBlank()) {
            throw new IllegalArgumentException("Region missing for Tencent Cloud request");
        }
        Credential cred = new Credential(secretId, secretKey);
        this.client = new TtsClient(cred, region);
        this.rateLimiter = rateLimiter;
    }

    public TencentTtsResult synthesize(int voiceType, String text) {
        // Acquire rate limit permit before making request (if rate limiter configured)
        if (rateLimiter != null) {
            try {
                boolean acquired = rateLimiter.acquire(RATE_LIMIT_ACQUIRE_TIMEOUT);
                if (!acquired) {
                    throw new TencentTtsClientException(
                            "Rate limit timeout - provider overwhelmed after waiting "
                                    + RATE_LIMIT_ACQUIRE_TIMEOUT.toSeconds()
                                    + "s",
                            null);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new TencentTtsClientException("Interrupted while waiting for rate limit", e);
            }
        }

        try {
            TextToVoiceRequest req = new TextToVoiceRequest();
            req.setText(text);
            req.setSessionId(UUID.randomUUID().toString());
            req.setVoiceType((long) voiceType);
            req.setCodec("mp3");
            req.setSampleRate(16000L);

            TextToVoiceResponse resp = client.TextToVoice(req);

            // Notify success for rate recovery
            if (rateLimiter != null) {
                rateLimiter.notifySuccess();
            }

            return new TencentTtsResult(resp.getAudio(), resp.getSessionId());
        } catch (TencentCloudSDKException e) {
            // Check if this is a rate limit error (Tencent uses various error codes)
            String errorCode = e.getErrorCode();
            if (errorCode != null
                    && (errorCode.contains("RequestLimitExceeded")
                            || errorCode.contains("RateLimitExceeded"))) {
                if (rateLimiter != null) {
                    log.warn("[TencentTTS] Rate limit hit: {}", e.getMessage());
                    rateLimiter.notifyRateLimited(null);
                }
            }
            throw new TencentTtsClientException("Tencent TTS API error", e);
        }
    }
}
