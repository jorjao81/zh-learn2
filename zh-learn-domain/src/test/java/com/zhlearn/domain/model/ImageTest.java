package com.zhlearn.domain.model;

import static org.assertj.core.api.Assertions.*;

import java.net.URI;
import java.util.Optional;

import org.junit.jupiter.api.Test;

class ImageTest {

    @Test
    void shouldCreateValidImage() {
        Image image =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.of("Test image"),
                        800,
                        600,
                        "image/jpeg");

        assertThat(image.sourceUrl()).isEqualTo(URI.create("https://example.com/image.jpg"));
        assertThat(image.thumbnailUrl()).isEqualTo("https://example.com/thumb.jpg");
        assertThat(image.title()).isEqualTo(Optional.of("Test image"));
        assertThat(image.width()).isEqualTo(800);
        assertThat(image.height()).isEqualTo(600);
        assertThat(image.contentType()).isEqualTo("image/jpeg");
    }

    @Test
    void shouldCreateImageWithEmptyTitle() {
        Image image =
                new Image(
                        URI.create("https://example.com/image.jpg"),
                        "https://example.com/thumb.jpg",
                        Optional.empty(),
                        800,
                        600,
                        "image/jpeg");

        assertThat(image.title()).isEmpty();
    }

    @Test
    void shouldThrowExceptionForNullSourceUrl() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        null,
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        800,
                                        600,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image source URL cannot be null");
    }

    @Test
    void shouldThrowExceptionForNullThumbnailUrl() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        null,
                                        Optional.of("Test"),
                                        800,
                                        600,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image thumbnail URL cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForEmptyThumbnailUrl() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "   ",
                                        Optional.of("Test"),
                                        800,
                                        600,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image thumbnail URL cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForNullTitle() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        null,
                                        800,
                                        600,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image title cannot be null (use Optional.empty())");
    }

    @Test
    void shouldThrowExceptionForZeroWidth() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        0,
                                        600,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image width must be positive, got: 0");
    }

    @Test
    void shouldThrowExceptionForNegativeWidth() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        -100,
                                        600,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image width must be positive, got: -100");
    }

    @Test
    void shouldThrowExceptionForZeroHeight() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        800,
                                        0,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image height must be positive, got: 0");
    }

    @Test
    void shouldThrowExceptionForNegativeHeight() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        800,
                                        -50,
                                        "image/jpeg"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image height must be positive, got: -50");
    }

    @Test
    void shouldThrowExceptionForNullContentType() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        800,
                                        600,
                                        null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image content type cannot be null or empty");
    }

    @Test
    void shouldThrowExceptionForEmptyContentType() {
        assertThatThrownBy(
                        () ->
                                new Image(
                                        URI.create("https://example.com/image.jpg"),
                                        "https://example.com/thumb.jpg",
                                        Optional.of("Test"),
                                        800,
                                        600,
                                        "   "))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Image content type cannot be null or empty");
    }
}
