package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.arizona.ece373.editor.engine.EditTestFormats.BOLD;
import static edu.arizona.ece373.editor.engine.EditTestFormats.PLAIN;
import static org.junit.jupiter.api.Assertions.*;

/** Tests for InsertTextCommand, DeleteTextCommand and ApplyFormatCommand. */
class TextCommandsTest {

    private Paragraph mixed() {
        return new Paragraph(List.of(new TextRun("one ", PLAIN), new TextRun("two", BOLD),
                new TextRun(" three", PLAIN)));
    }

    // Insert

    @Test
    void insertAtStartMiddleAndEnd() {
        Paragraph p = new Paragraph("ac", PLAIN);
        new InsertTextCommand(p, 1, "b").execute();
        new InsertTextCommand(p, 0, "[").execute();
        new InsertTextCommand(p, 4, "]").execute();
        assertEquals("[abc]", p.getText());
    }

    @Test
    void insertUndoRemovesOnlyTheInsertedText() {
        Paragraph p = mixed();
        List<TextRun> original = p.getRuns();
        EditCommand insert = new InsertTextCommand(p, 4, "big ");
        insert.execute();
        assertEquals("one big two three", p.getText());
        insert.undo();
        assertEquals(original, p.getRuns());
    }

    @Test
    void insertRunsKeepsFormattingAndUndoes() {
        Paragraph p = new Paragraph("ab", PLAIN);
        EditCommand insert = new InsertTextCommand(p, 1, List.of(new TextRun("X", BOLD)));
        insert.execute();
        assertEquals(BOLD, p.getRuns().get(1).getFormat());
        insert.undo();
        assertEquals(List.of(new TextRun("ab", PLAIN)), p.getRuns());
    }

    @Test
    void insertOutsideParagraphFailsWithoutChanges() {
        Paragraph p = new Paragraph("ab", PLAIN);
        assertThrows(IndexOutOfBoundsException.class, () -> new InsertTextCommand(p, 3, "x").execute());
        assertThrows(IndexOutOfBoundsException.class, () -> new InsertTextCommand(p, -1, "x").execute());
        assertEquals("ab", p.getText());
    }

    // Delete

    @Test
    void deleteAcrossRunsAndUndoRestoresFormatting() {
        Paragraph p = mixed();
        List<TextRun> original = p.getRuns();
        EditCommand delete = new DeleteTextCommand(p, 2, 9);
        delete.execute();
        assertEquals("onhree", p.getText());
        delete.undo();
        assertEquals(original, p.getRuns());
    }

    @Test
    void deleteWholeParagraphAndEmptyRange() {
        Paragraph p = new Paragraph("abc", PLAIN);
        EditCommand all = new DeleteTextCommand(p, 0, 3);
        all.execute();
        assertEquals("", p.getText());
        all.undo();
        assertEquals("abc", p.getText());
        new DeleteTextCommand(p, 1, 1).execute();
        assertEquals("abc", p.getText());
    }

    @Test
    void deleteRejectsBadRanges() {
        Paragraph p = new Paragraph("abc", PLAIN);
        assertThrows(IllegalArgumentException.class, () -> new DeleteTextCommand(p, 2, 1));
        assertThrows(IndexOutOfBoundsException.class, () -> new DeleteTextCommand(p, 1, 9).execute());
        assertEquals("abc", p.getText());
    }

    // Apply format

    @Test
    void formatChangesOnlyTheRangeAndNotTheText() {
        Paragraph p = new Paragraph("Hello world", PLAIN);
        new ApplyFormatCommand(p, 6, 11, EditTestFormats::bold).execute();
        assertEquals("Hello world", p.getText());
        assertEquals(List.of(new TextRun("Hello ", PLAIN), new TextRun("world", BOLD)), p.getRuns());
    }

    @Test
    void formatKeepsOtherAttributesOfEachRun() {
        CharacterFormat redItalic = new CharacterFormat(false, true, 14, "Times", "red");
        Paragraph p = new Paragraph("abc", redItalic);
        new ApplyFormatCommand(p, 0, 3, EditTestFormats::bold).execute();
        CharacterFormat f = p.getRuns().get(0).getFormat();
        assertTrue(f.isBold());
        assertTrue(f.isItalic());
        assertEquals("red", f.getColor());
        assertEquals(14, f.getSize());
    }

    @Test
    void formatUndoRestoresPreviousFormatting() {
        Paragraph p = mixed();
        List<TextRun> original = p.getRuns();
        EditCommand format = new ApplyFormatCommand(p, 0, p.countChars(),
                f -> new CharacterFormat(f.isBold(), f.isItalic(), f.getSize(), f.getFont(), "blue"));
        format.execute();
        assertEquals("blue", p.getRuns().get(0).getFormat().getColor());
        format.undo();
        assertEquals(original, p.getRuns());
    }

    @Test
    void formatOutsideParagraphFailsWithoutChanges() {
        Paragraph p = new Paragraph("abc", PLAIN);
        assertThrows(IndexOutOfBoundsException.class,
                () -> new ApplyFormatCommand(p, 1, 9, EditTestFormats::bold).execute());
        assertEquals(List.of(new TextRun("abc", PLAIN)), p.getRuns());
    }

    @Test
    void formatWorksOnSectionTitle() {
        Section section = new Section("Intro", PLAIN);
        new ApplyFormatCommand(section.getTitle(), 0, 5, EditTestFormats::bold).execute();
        assertTrue(section.getTitle().getRuns().get(0).getFormat().isBold());
    }
}
