package com.zhlearn.infrastructure.dictionary;

import com.zhlearn.domain.model.WordAnalysis;
import com.zhlearn.infrastructure.pleco.PlecoEntry;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PlecoExportDictionaryTest {

    @Test
    void lookupReturnsAnalysisFromEntries() {
        PlecoEntry entry = new PlecoEntry("瞬", "shùn", "verb wink; twinkle");
        PlecoExportDictionary dict = new PlecoExportDictionary(List.of(entry));

        assertEquals("pleco-export", dict.getName());
        Optional<WordAnalysis> maybe = dict.lookup("瞬");
        assertTrue(maybe.isPresent());
        WordAnalysis wa = maybe.get();
        assertEquals("瞬", wa.word().characters());
        assertEquals("shùn", wa.pinyin().pinyin());
        assertEquals("verb wink; twinkle", wa.definition().meaning());
    }

    @Test
    void lookupIsEmptyWhenNotPresent() {
        PlecoExportDictionary dict = new PlecoExportDictionary(List.of());
        assertTrue(dict.lookup("不存在").isEmpty());
    }
}

