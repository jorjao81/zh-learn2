package com.zhlearn.infrastructure.pleco;

import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PlecoExportParserTest {

    @Test
    void parsesBasicTsvWithSequenceArrow() throws Exception {
        String data = "1→瞬\tshun4\tverb wink; twinkle\n" +
                      "2→胸\txiong1\tnoun chest\n";
        PlecoExportParser parser = new PlecoExportParser();
        List<PlecoEntry> entries = parser.parseFromReader(new StringReader(data));

        assertEquals(2, entries.size());
        PlecoEntry e1 = entries.get(0);
        assertEquals("瞬", e1.hanzi());
        assertEquals("shùn", e1.pinyin());
        assertEquals("verb wink; twinkle", e1.definitionText());

        PlecoEntry e2 = entries.get(1);
        assertEquals("胸", e2.hanzi());
        assertEquals("xiōng", e2.pinyin());
        assertEquals("noun chest", e2.definitionText());
    }

    @Test
    void handlesBomInFirstColumn() throws Exception {
        String data = "\uFEFF3→然\tran2\tverb burn\n";
        PlecoExportParser parser = new PlecoExportParser();
        List<PlecoEntry> entries = parser.parseFromReader(new StringReader(data));

        assertEquals(1, entries.size());
        PlecoEntry e = entries.get(0);
        assertEquals("然", e.hanzi());
        assertEquals("rán", e.pinyin());
        assertEquals("verb burn", e.definitionText());
    }

    @Test
    void skipsInvalidRows() throws Exception {
        String data = "not-enough-cols\n" +
                      "4→\t\t\n" +
                      "5→冗\t\tdefinition only\n" +
                      "6→燃\tran2\tverb burn\n";
        PlecoExportParser parser = new PlecoExportParser();
        List<PlecoEntry> entries = parser.parseFromReader(new StringReader(data));

        assertEquals(1, entries.size());
        assertEquals("燃", entries.get(0).hanzi());
    }
}

