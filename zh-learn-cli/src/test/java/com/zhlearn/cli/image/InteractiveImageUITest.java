package com.zhlearn.cli.image;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.junit.jupiter.api.Test;

import com.zhlearn.application.image.ImageSelectionSession;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ImageCandidate;

class InteractiveImageUITest {

    @Test
    void shouldReturnSelectedImagesWhenSpaceThenEnterPressed() throws IOException {
        Terminal terminal = buildTerminal("xterm", " \n");
        InteractiveImageUI ui =
                new InteractiveImageUI(
                        HttpClient.newBuilder().build(), reuseOnce(terminal), name -> "xterm");

        ImageCandidate candidate = createCandidate();
        ImageSelectionSession session = new ImageSelectionSession(List.of(candidate));

        List<ImageCandidate> selected = ui.run(session, new Hanzi("学"));

        assertThat(selected).containsExactly(candidate);
    }

    @Test
    void shouldReturnNullWhenTerminalIsDumb() throws IOException {
        Terminal terminal = buildTerminal(Terminal.TYPE_DUMB, "");
        InteractiveImageUI ui =
                new InteractiveImageUI(
                        HttpClient.newBuilder().build(), reuseOnce(terminal), name -> "xterm");

        ImageSelectionSession session = new ImageSelectionSession(List.of(createCandidate()));

        assertThat(ui.run(session, new Hanzi("学"))).isNull();
    }

    private Terminal buildTerminal(String type, String input) throws IOException {
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        return TerminalBuilder.builder()
                .name("test")
                .type(type)
                .system(false)
                .streams(in, out)
                .build();
    }

    private Supplier<Terminal> reuseOnce(Terminal terminal) {
        return new Supplier<>() {
            private boolean used;

            @Override
            public Terminal get() {
                if (used) {
                    throw new IllegalStateException("Terminal already supplied");
                }
                used = true;
                return terminal;
            }
        };
    }

    private ImageCandidate createCandidate() {
        Image image =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.of("Image"),
                        800,
                        600,
                        "image/jpeg");
        return new ImageCandidate(image, Path.of("/tmp/source.jpg"));
    }
}
