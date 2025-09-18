package com.zhlearn.infrastructure.qwen;

import java.io.IOException;

public interface QwenTtsClient {
    QwenTtsResult synthesize(String apiKey, String model, String voice, String text)
        throws IOException, InterruptedException;
}
