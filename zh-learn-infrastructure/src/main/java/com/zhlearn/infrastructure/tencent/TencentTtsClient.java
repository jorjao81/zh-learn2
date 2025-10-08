package com.zhlearn.infrastructure.tencent;

import java.util.UUID;

import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;

class TencentTtsClient {
    private final TtsClient client;

    TencentTtsClient(TtsClient client) {
        this.client = client;
    }

    TencentTtsClient(String secretId, String secretKey, String region) {
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
    }

    public TencentTtsResult synthesize(int voiceType, String text) {
        try {
            TextToVoiceRequest req = new TextToVoiceRequest();
            req.setText(text);
            req.setSessionId(UUID.randomUUID().toString());
            req.setVoiceType((long) voiceType);
            req.setCodec("mp3");
            req.setSampleRate(16000L);

            TextToVoiceResponse resp = client.TextToVoice(req);
            return new TencentTtsResult(resp.getAudio(), resp.getSessionId());
        } catch (TencentCloudSDKException e) {
            throw new RuntimeException("Tencent TTS API error", e);
        }
    }
}
