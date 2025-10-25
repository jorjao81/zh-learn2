package com.zhlearn.infrastructure.dummy;

import java.net.URI;
import java.util.List;
import java.util.Optional;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.ImageProvider;

public class DummyImageProvider implements ImageProvider {

    @Override
    public String getName() {
        return "dummy";
    }

    @Override
    public String getDescription() {
        return "Test provider that returns dummy images for development and testing";
    }

    @Override
    public ProviderType getType() {
        return ProviderType.DUMMY;
    }

    @Override
    public List<Image> searchImages(Hanzi word, Definition definition, int maxResults) {
        // Return hardcoded test images
        List<Image> allImages =
                List.of(
                        new Image(
                                URI.create("https://example.com/image1.jpg"),
                                "https://example.com/thumb1.jpg",
                                Optional.of("Dummy image 1 for " + word.characters()),
                                800,
                                600,
                                "image/jpeg"),
                        new Image(
                                URI.create("https://example.com/image2.jpg"),
                                "https://example.com/thumb2.jpg",
                                Optional.of("Dummy image 2 for " + word.characters()),
                                1024,
                                768,
                                "image/jpeg"),
                        new Image(
                                URI.create("https://example.com/image3.png"),
                                "https://example.com/thumb3.png",
                                Optional.of("Dummy image 3 for " + word.characters()),
                                640,
                                480,
                                "image/png"),
                        new Image(
                                URI.create("https://example.com/image4.webp"),
                                "https://example.com/thumb4.webp",
                                Optional.empty(),
                                1280,
                                720,
                                "image/webp"),
                        new Image(
                                URI.create("https://example.com/image5.jpg"),
                                "https://example.com/thumb5.jpg",
                                Optional.of("Dummy image 5 for " + word.characters()),
                                1920,
                                1080,
                                "image/jpeg"));

        // Return only up to maxResults
        return allImages.subList(0, Math.min(maxResults, allImages.size()));
    }
}
