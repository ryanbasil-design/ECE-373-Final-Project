package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ParagraphTest {

    private final CharacterFormat plain = new CharacterFormat(false, false, 12, "Arial", "black");
    private final CharacterFormat bold = new CharacterFormat(true, false, 12, "Arial", "black");

    private Paragraph hollyTheTiger() {
        return new Paragraph(List.of(
                new TextRun("Holly ", plain),
                new TextRun("the", bold),
                new TextRun(" tiger", plain)));
    }

    // Constructors

    @Test
    void emptyParagraphHasNoRuns() {
        Paragraph p = new Paragraph();
        assertTrue(p.getRuns().isEmpty());
    }

    @Test
    void listConstructorDropsEmptyRunsAndMergesNeighbors() {
        Paragraph p = new Paragraph(List.of(
                new TextRun("a", plain),
                new TextRun("", bold),
                new TextRun("b", plain)));
        assertEquals(List.of(new TextRun("ab", plain)), p.getRuns());
    }

    @Test
    void getRunsCannotBeModified() {
        Paragraph p = hollyTheTiger();
        assertThrows(UnsupportedOperationException.class,
                () -> p.getRuns().add(new TextRun("x", plain)));
    }

    // Insert

    @Test
    void insertInMiddleOfRun() {
        Paragraph p = hollyTheTiger();
        p.insertText(2, "LL");
        assertEquals("HoLLlly the tiger", p.getText());
        assertEquals(List.of(
                new TextRun("HoLLlly ", plain),
                new TextRun("the", bold),
                new TextRun(" tiger", plain)), p.getRuns());
    }

    @Test
    void insertAtBoundaryTakesFormatBefore() {
        Paragraph p = hollyTheTiger();
        p.insertText(9, " tall");
        assertEquals("Holly the tall tiger", p.getText());
        assertEquals(new TextRun("the tall", bold), p.getRuns().get(1));
    }

    @Test
    void insertAtStartUsesFirstRunFormat() {
        Paragraph p = hollyTheTiger();
        p.insertText(0, "!");
        assertEquals(new TextRun("!Holly ", plain), p.getRuns().get(0));
    }

    @Test
    void insertAtEnd() {
        Paragraph p = hollyTheTiger();
        p.insertText(15, "!");
        assertEquals("Holly the tiger!", p.getText());
        assertEquals(new TextRun(" tiger!", plain), p.getRuns().get(2));
    }

    @Test
    void insertIntoEmptyParagraphUsesDefaultFormat() {
        Paragraph p = new Paragraph();
        p.insertText(0, "Hi");
        assertEquals(List.of(new TextRun("Hi", CharacterFormat.DEFAULT)), p.getRuns());
    }

    @Test
    void insertOutsideParagraphThrows() {
        Paragraph p = hollyTheTiger();
        assertThrows(IndexOutOfBoundsException.class, () -> p.insertText(-1, "x"));
        assertThrows(IndexOutOfBoundsException.class, () -> p.insertText(16, "x"));
    }

    // Delete

    @Test
    void deleteWithinOneRun() {
        Paragraph p = hollyTheTiger();
        List<TextRun> removed = p.deleteText(1, 2);
        assertEquals("Hly the tiger", p.getText());
        assertEquals(List.of(new TextRun("ol", plain)), removed);
    }

    @Test
    void deleteAcrossTwoRuns() {
        Paragraph p = hollyTheTiger();
        List<TextRun> removed = p.deleteText(4, 4);
        assertEquals("Holle tiger", p.getText());
        assertEquals(List.of(
                new TextRun("Holl", plain),
                new TextRun("e", bold),
                new TextRun(" tiger", plain)), p.getRuns());
        assertEquals(List.of(
                new TextRun("y ", plain),
                new TextRun("th", bold)), removed);
    }

    @Test
    void deleteWholeMiddleRunMergesNeighbors() {
        Paragraph p = hollyTheTiger();
        p.deleteText(6, 3);
        assertEquals(List.of(new TextRun("Holly  tiger", plain)), p.getRuns());
    }

    @Test
    void deleteEverything() {
        Paragraph p = hollyTheTiger();
        p.deleteText(0, 15);
        assertEquals("", p.getText());
        assertTrue(p.getRuns().isEmpty());
    }

    @Test
    void invalidDeleteThrows() {
        Paragraph p = hollyTheTiger();
        assertThrows(IllegalArgumentException.class, () -> p.deleteText(0, -1));
        assertThrows(IndexOutOfBoundsException.class, () -> p.deleteText(-1, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> p.deleteText(10, 10));
    }

    // Counts

    @Test
    void countsForMixedParagraph() {
        Paragraph p = hollyTheTiger();
        assertEquals(3, p.countWords());
        assertEquals(15, p.countChars());
        assertEquals(1, p.countParagraphs());
    }

    @Test
    void extraSpacesDoNotAddWords() {
        assertEquals(2, new Paragraph("  one   two  ", plain).countWords());
    }

    @Test
    void emptyParagraphCountsZero() {
        Paragraph p = new Paragraph();
        assertEquals(0, p.countWords());
        assertEquals(0, p.countChars());
    }
}
