package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.arizona.ece373.editor.engine.EditTestFormats.BOLD;
import static edu.arizona.ece373.editor.engine.EditTestFormats.PLAIN;
import static org.junit.jupiter.api.Assertions.*;

/** Tests for AddElementCommand, SplitParagraphCommand and MergeParagraphsCommand. */
class StructureCommandsTest {

    // Add element

    @Test
    void addElementToDocumentAndUndo() {
        Document doc = new Document();
        Paragraph p = new Paragraph("hi", PLAIN);
        EditCommand add = new AddElementCommand(ElementContainer.of(doc), p);
        add.execute();
        assertEquals(List.of(p), doc.getElements());
        add.undo();
        assertTrue(doc.getElements().isEmpty());
    }

    @Test
    void addNestedSectionsToAnyDepth() {
        Document doc = new Document();
        Section outer = new Section("Outer", PLAIN);
        Section inner = new Section("Inner", PLAIN);
        Paragraph p = new Paragraph("deep", PLAIN);
        new AddElementCommand(ElementContainer.of(doc), outer).execute();
        new AddElementCommand(ElementContainer.of(outer), inner).execute();
        new AddElementCommand(ElementContainer.of(inner), p).execute();
        assertEquals(1, doc.countParagraphs());
        assertSame(p, inner.getChildren().get(0));
    }

    @Test
    void addAtIndexAndRejectBadIndex() {
        Section s = new Section("S", PLAIN);
        Paragraph a = new Paragraph("a", PLAIN);
        Paragraph b = new Paragraph("b", PLAIN);
        new AddElementCommand(ElementContainer.of(s), b).execute();
        new AddElementCommand(ElementContainer.of(s), a, 0).execute();
        assertEquals(List.of(a, b), s.getChildren());
        assertThrows(IndexOutOfBoundsException.class,
                () -> new AddElementCommand(ElementContainer.of(s), new Paragraph(), 5).execute());
    }

    @Test
    void sectionCannotBeAddedInsideItself() {
        Section s = new Section("S", PLAIN);
        assertThrows(IllegalArgumentException.class,
                () -> new AddElementCommand(ElementContainer.of(s), s).execute());
    }

    // Split

    @Test
    void splitMovesTailIntoNewParagraphAfterOriginal() {
        Document doc = new Document();
        Paragraph p = new Paragraph(List.of(new TextRun("Hello ", PLAIN), new TextRun("world", BOLD)));
        Paragraph after = new Paragraph("after", PLAIN);
        doc.add(p);
        doc.add(after);
        new SplitParagraphCommand(ElementContainer.of(doc), p, 6).execute();
        assertEquals(3, doc.getElements().size());
        assertEquals("Hello ", p.getText());
        Paragraph created = (Paragraph) doc.getElements().get(1);
        assertEquals(List.of(new TextRun("world", BOLD)), created.getRuns());
        assertSame(after, doc.getElements().get(2));
    }

    @Test
    void splitUndoRejoinsParagraph() {
        Section s = new Section("S", PLAIN);
        Paragraph p = new Paragraph(List.of(new TextRun("ab", PLAIN), new TextRun("cd", BOLD)));
        s.add(p);
        List<TextRun> original = p.getRuns();
        EditCommand split = new SplitParagraphCommand(ElementContainer.of(s), p, 3);
        split.execute();
        split.undo();
        assertEquals(List.of(p), s.getChildren());
        assertEquals(original, p.getRuns());
    }

    @Test
    void splitAtEndsMakesEmptyParagraph() {
        Document doc = new Document();
        Paragraph p = new Paragraph("abc", PLAIN);
        doc.add(p);
        new SplitParagraphCommand(ElementContainer.of(doc), p, 3).execute();
        assertEquals("", doc.getElements().get(1).getText());
        new SplitParagraphCommand(ElementContainer.of(doc), p, 0).execute();
        assertEquals("", p.getText());
        assertEquals("abc", doc.getElements().get(1).getText());
    }

    @Test
    void splitRejectsBadPositionWithoutChanges() {
        Document doc = new Document();
        Paragraph p = new Paragraph("abc", PLAIN);
        doc.add(p);
        assertThrows(IndexOutOfBoundsException.class,
                () -> new SplitParagraphCommand(ElementContainer.of(doc), p, 4).execute());
        assertEquals(1, doc.getElements().size());
        assertEquals("abc", p.getText());
    }

    // Merge

    @Test
    void mergeJoinsWithNextParagraphAndUndoSplitsBack() {
        Document doc = new Document();
        Paragraph a = new Paragraph("Hello ", PLAIN);
        Paragraph b = new Paragraph("world", BOLD);
        doc.add(a);
        doc.add(b);
        EditCommand merge = new MergeParagraphsCommand(ElementContainer.of(doc), a);
        merge.execute();
        assertEquals(List.of(a), doc.getElements());
        assertEquals(List.of(new TextRun("Hello ", PLAIN), new TextRun("world", BOLD)), a.getRuns());
        merge.undo();
        assertEquals(List.of(a, b), doc.getElements());
        assertEquals("Hello ", a.getText());
        assertEquals("world", b.getText());
    }

    @Test
    void mergeLastParagraphFails() {
        Document doc = new Document();
        Paragraph a = new Paragraph("only", PLAIN);
        doc.add(a);
        assertThrows(IllegalArgumentException.class,
                () -> new MergeParagraphsCommand(ElementContainer.of(doc), a).execute());
        assertEquals("only", a.getText());
    }

    @Test
    void mergeWithSectionFails() {
        Document doc = new Document();
        Paragraph a = new Paragraph("a", PLAIN);
        doc.add(a);
        doc.add(new Section("S", PLAIN));
        assertThrows(IllegalArgumentException.class,
                () -> new MergeParagraphsCommand(ElementContainer.of(doc), a).execute());
        assertEquals(2, doc.getElements().size());
    }
}
