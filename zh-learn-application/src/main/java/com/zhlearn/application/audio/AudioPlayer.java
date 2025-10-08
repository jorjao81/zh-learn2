package com.zhlearn.application.audio;

import java.nio.file.Path;

public interface AudioPlayer {
    void play(Path file);

    void stop();
}
