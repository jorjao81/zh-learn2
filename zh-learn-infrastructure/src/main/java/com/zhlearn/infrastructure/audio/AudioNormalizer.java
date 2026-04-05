package com.zhlearn.infrastructure.audio;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class AudioNormalizer {

    public AudioNormalizer() {}

    public void normalizeToMp3(Path input, Path output) throws IOException, InterruptedException {
        if (input == null || !Files.exists(input))
            throw new IOException("input not found: " + input);
        Files.createDirectories(output.getParent());

        // Allow disabling external tools in tests/CI (env var or system property)
        String disable = System.getProperty("zhlearn.disable.ffmpeg");
        if (disable == null) disable = System.getenv("ZHLEARN_DISABLE_FFMPEG");
        if (disable != null && (disable.equals("1") || disable.equalsIgnoreCase("true"))) {
            Files.copy(input, output, StandardCopyOption.REPLACE_EXISTING);
            return;
        }

        // Prefer ffmpeg on all OS; mac users likely have it via brew
        if (isOnPath("ffmpeg")) {
            ProcessBuilder pb =
                    new ProcessBuilder(
                            "ffmpeg",
                            "-y",
                            "-i",
                            input.toAbsolutePath().toString(),
                            "-af",
                            "loudnorm=I=-16:LRA=11:TP=-1.5,areverse,atrim=start=0.01,areverse",
                            "-ar",
                            "44100",
                            "-ac",
                            "1",
                            "-codec:a",
                            "libmp3lame",
                            "-b:a",
                            "128k",
                            output.toAbsolutePath().toString());
            pb.redirectError(ProcessBuilder.Redirect.DISCARD);
            Process p = pb.start();
            int code = p.waitFor();
            if (code == 0 && Files.exists(output)) {
                return;
            }
            throw new IOException(
                    "ffmpeg normalization failed with exit code " + code + " for: " + input);
        } else {
            throw new IOException(
                    "ffmpeg not found on PATH. Install it (e.g. 'brew install ffmpeg') "
                            + "or set ZHLEARN_DISABLE_FFMPEG=1 to skip normalization.");
        }
    }

    private boolean isOnPath(String tool) {
        try {
            Process p = new ProcessBuilder(tool, "-version").start();
            p.destroy();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
