package com.zhlearn.domain.provider;

import java.util.List;

import com.zhlearn.domain.model.Definition;
import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Image;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;

/** Provides representative images for a given Chinese word and its definition. */
public interface ImageProvider {
    String getName();

    String getDescription();

    ProviderType getType();

    /**
     * Search for images representing the given word and definition.
     *
     * @param word the Chinese word to search for
     * @param definition the English definition to help contextualize the search
     * @param maxResults maximum number of images to return
     * @return list of images matching the search criteria
     */
    List<Image> searchImages(Hanzi word, Definition definition, int maxResults);
}
