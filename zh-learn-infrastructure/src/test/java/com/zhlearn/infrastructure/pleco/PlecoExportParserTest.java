package com.zhlearn.infrastructure.pleco;

import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;

class PlecoExportParserTest {

    @Test
    void parsesBasicTsvWithoutSequenceArrow() throws Exception {
        String data = "瞬\tshun4\tverb wink; twinkle\n" + "胸\txiong1\tnoun chest\n";
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
        String data = "\uFEFF然\tran2\tverb burn\n";
        PlecoExportParser parser = new PlecoExportParser();
        List<PlecoEntry> entries = parser.parseFromReader(new StringReader(data));

        assertEquals(1, entries.size());
        PlecoEntry e = entries.get(0);
        assertEquals("然", e.hanzi());
        assertEquals("rán", e.pinyin());
        assertEquals("verb burn", e.definitionText());
    }

    @Test
    void crashesOnInvalidRows() throws Exception {
        // Test that parser crashes on invalid records (Constitutional compliance)
        String dataWithOneColumn = "not-enough-cols\n";
        PlecoExportParser parser = new PlecoExportParser();

        IllegalArgumentException exception =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            parser.parseFromReader(new StringReader(dataWithOneColumn));
                        });
        assertTrue(exception.getMessage().contains("Record must have at least 2 columns"));

        // Test that parser crashes on too many columns
        String dataWithTooManyColumns = "汉字\tpinyin\tdefinition\textra\n";
        IllegalArgumentException exception2 =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            parser.parseFromReader(new StringReader(dataWithTooManyColumns));
                        });
        assertTrue(exception2.getMessage().contains("Record must have at most 3 columns"));

        // Test that parser crashes on empty hanzi
        String dataWithEmptyHanzi = "\tpinyin\tdefinition\n";
        IllegalArgumentException exception3 =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            parser.parseFromReader(new StringReader(dataWithEmptyHanzi));
                        });
        assertTrue(exception3.getMessage().contains("Invalid or empty hanzi"));

        // Test that parser crashes on empty pinyin
        String dataWithEmptyPinyin = "汉字\t\tdefinition\n";
        IllegalArgumentException exception4 =
                assertThrows(
                        IllegalArgumentException.class,
                        () -> {
                            parser.parseFromReader(new StringReader(dataWithEmptyPinyin));
                        });
        assertTrue(exception4.getMessage().contains("Invalid or empty pinyin"));
    }

    @Test
    void parsesTwoColumnFormatWithEmptyDefinitions() throws Exception {
        // Test parsing flash card format without definitions
        String data = "人类历史\tren2lei4 li4shi3\n" + "残暴不仁\tcan2bao4 bu4ren2\n" + "善射\tshan4 she4\n";
        PlecoExportParser parser = new PlecoExportParser();
        List<PlecoEntry> entries = parser.parseFromReader(new StringReader(data));

        assertEquals(3, entries.size());

        PlecoEntry e1 = entries.get(0);
        assertEquals("人类历史", e1.hanzi());
        assertEquals("rénlèi lìshǐ", e1.pinyin());
        assertEquals("", e1.definitionText()); // Empty definition

        PlecoEntry e2 = entries.get(1);
        assertEquals("残暴不仁", e2.hanzi());
        assertEquals("cánbào bùrén", e2.pinyin());
        assertEquals("", e2.definitionText()); // Empty definition

        PlecoEntry e3 = entries.get(2);
        assertEquals("善射", e3.hanzi());
        assertEquals("shàn shè", e3.pinyin());
        assertEquals("", e3.definitionText()); // Empty definition
    }

    @Test
    void parsesMixedTwoAndThreeColumnFormat() throws Exception {
        // Test parsing mixed format (some with definitions, some without)
        String data =
                "人类历史\tren2lei4 li4shi3\n"
                        + "遗腹子\tyi2fu4zi3\tnoun posthumous child\n"
                        + "善射\tshan4 she4\n"
                        + "作揖\tzuo4//yi1\tverb slight bow with hands clasped in front\n";
        PlecoExportParser parser = new PlecoExportParser();
        List<PlecoEntry> entries = parser.parseFromReader(new StringReader(data));

        assertEquals(4, entries.size());

        // Entry without definition
        PlecoEntry e1 = entries.get(0);
        assertEquals("人类历史", e1.hanzi());
        assertEquals("rénlèi lìshǐ", e1.pinyin());
        assertEquals("", e1.definitionText());

        // Entry with definition
        PlecoEntry e2 = entries.get(1);
        assertEquals("遗腹子", e2.hanzi());
        assertEquals("yífùzǐ", e2.pinyin());
        assertEquals("noun posthumous child", e2.definitionText());

        // Entry without definition
        PlecoEntry e3 = entries.get(2);
        assertEquals("善射", e3.hanzi());
        assertEquals("shàn shè", e3.pinyin());
        assertEquals("", e3.definitionText());

        // Entry with definition
        PlecoEntry e4 = entries.get(3);
        assertEquals("作揖", e4.hanzi());
        assertEquals("zuò//yī", e4.pinyin());
        assertEquals("verb slight bow with hands clasped in front", e4.definitionText());
    }
}
