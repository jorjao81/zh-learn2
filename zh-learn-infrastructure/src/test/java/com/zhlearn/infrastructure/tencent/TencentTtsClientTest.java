package com.zhlearn.infrastructure.tencent;

import com.tencentcloudapi.common.exception.TencentCloudSDKException;
import com.tencentcloudapi.tts.v20190823.TtsClient;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceRequest;
import com.tencentcloudapi.tts.v20190823.models.TextToVoiceResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TencentTtsClientTest {

    @Mock
    private TtsClient sdkClient;

    private TencentTtsClient tencentTtsClient;

    @BeforeEach
    void setUp() {
        tencentTtsClient = new TencentTtsClient(sdkClient);
    }

    @Test
    void synthesizeSuccessfully() throws TencentCloudSDKException {
        // Arrange
        TextToVoiceResponse response = new TextToVoiceResponse();
        response.setAudio("fake-audio-data");
        response.setSessionId("test-session-123");

        when(sdkClient.TextToVoice(any(TextToVoiceRequest.class))).thenReturn(response);

        // Act
        TencentTtsResult result = tencentTtsClient.synthesize(101052, "学习");

        // Assert
        assertThat(result.audioData()).isEqualTo("fake-audio-data");
        assertThat(result.sessionId()).isEqualTo("test-session-123");

        ArgumentCaptor<TextToVoiceRequest> captor = ArgumentCaptor.forClass(TextToVoiceRequest.class);
        verify(sdkClient).TextToVoice(captor.capture());
        TextToVoiceRequest capturedRequest = captor.getValue();
        assertThat(capturedRequest.getText()).isEqualTo("学习");
        assertThat(capturedRequest.getVoiceType()).isEqualTo(101052L);
    }

    @Test
    void handlesApiError() throws TencentCloudSDKException {
        // Arrange
        when(sdkClient.TextToVoice(any(TextToVoiceRequest.class)))
            .thenThrow(new TencentCloudSDKException("InvalidParameter"));

        // Act & Assert
        Throwable thrown = catchThrowable(() -> tencentTtsClient.synthesize(999999, "学习"));
        assertThat(thrown)
            .isInstanceOf(RuntimeException.class)
            .hasMessageContaining("Tencent TTS API error")
            .hasCauseInstanceOf(TencentCloudSDKException.class);
    }
}