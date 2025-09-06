package com.zhlearn.infrastructure.dummy;

import com.zhlearn.domain.model.Hanzi;
import com.zhlearn.domain.model.Pinyin;
import com.zhlearn.domain.model.ProviderInfo.ProviderType;
import com.zhlearn.domain.provider.AudioProvider;

import java.util.Optional;

public class DummyAudioProvider implements AudioProvider {
    
    @Override
    public String getName() {
        return "dummy";
    }
    
    @Override
    public String getDescription() {
        return "Test provider that returns dummy pronunciation for development and testing";
    }
    
    @Override
    public ProviderType getType() { return ProviderType.DUMMY; }
    
    @Override
    public Optional<String> getPronunciation(Hanzi word, Pinyin pinyin) {
        return Optional.of("[sound:dummy-" + word.characters() + ".mp3]");
    }
}
