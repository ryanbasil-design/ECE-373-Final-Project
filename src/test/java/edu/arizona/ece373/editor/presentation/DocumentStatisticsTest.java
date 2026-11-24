package edu.arizona.ece373.editor.presentation;

import edu.arizona.ece373.editor.engine.CharacterFormat;
import edu.arizona.ece373.editor.engine.Document;
import edu.arizona.ece373.editor.engine.Paragraph;
import edu.arizona.ece373.editor.engine.Section;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DocumentStatisticsTest {

    private final CharacterFormat plain = new CharacterFormat(false, false, 12, "Arial", "black");

    private Document buildDocument() {
        Section intro = new Section("Intro", plain);
        intro.add(new Paragraph("Hello world", plain));

        Document doc = new Document();
        doc.add(intro);
        doc.add(new Paragraph("The end.", plain));
        return doc;
    }

    @Test
    void countsAreCorrectRightAfterConstruction() {
        DocumentStatistics stats = new DocumentStatistics(buildDocument());
        assertEquals(5, stats.getWordCount());
        assertEquals(24, stats.getCharCount());
        assertEquals(2, stats.getParagraphCount());
    }

    @Test
    void emptyDocumentCountsZero() {
        DocumentStatistics stats = new DocumentStatistics(new Document());
        assertEquals(0, stats.getWordCount());
        assertEquals(0, stats.getCharCount());
        assertEquals(0, stats.getParagraphCount());
    }

    @Test
    void countsUpdateWhenDocumentNotifies() {
        Document doc = buildDocument();
        DocumentStatistics stats = new DocumentStatistics(doc);
        doc.addObserver(stats);

        doc.add(new Paragraph("More text", plain)); // +2 words, +9 chars, +1 paragraph
        doc.notifyObservers();

        assertEquals(7, stats.getWordCount());
        assertEquals(33, stats.getCharCount());
        assertEquals(3, stats.getParagraphCount());
    }

    @Test
    void countsDoNotChangeWithoutNotification() {
        Document doc = buildDocument();
        DocumentStatistics stats = new DocumentStatistics(doc);
        doc.addObserver(stats);

        doc.add(new Paragraph("More text", plain)); // edited, but nobody notified

        assertEquals(5, stats.getWordCount());
        assertEquals(24, stats.getCharCount());
        assertEquals(2, stats.getParagraphCount());
    }

    @Test
    void updatesAfterTextEditInsideParagraph() {
        Paragraph p = new Paragraph("Hello", plain);
        Document doc = new Document();
        doc.add(p);
        DocumentStatistics stats = new DocumentStatistics(doc);
        doc.addObserver(stats);

        p.insertText(5, " there");
        doc.notifyObservers();

        assertEquals(2, stats.getWordCount());
        assertEquals(11, stats.getCharCount());
    }

    @Test
    void reportShowsAllThreeCounts() {
        DocumentStatistics stats = new DocumentStatistics(buildDocument());
        assertEquals("Words: 5, Characters: 24, Paragraphs: 2", stats.getReport());
    }
}
