package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.Test;

import java.util.List;

import static edu.arizona.ece373.editor.engine.EditTestFormats.PLAIN;
import static org.junit.jupiter.api.Assertions.*;

class CompositeCommandTest {

    @Test
    void runsStepsInOrderAndUndoesAllAtOnce() {
        Document doc = new Document();
        Paragraph p = new Paragraph("cat cat", PLAIN);
        doc.add(p);
        CommandHistory history = new CommandHistory(doc);
        // replace-all style: later matches first so earlier positions stay valid
        history.execute(new CompositeCommand(List.of(
                new DeleteTextCommand(p, 4, 7), new InsertTextCommand(p, 4, "dog"),
                new DeleteTextCommand(p, 0, 3), new InsertTextCommand(p, 0, "dog"))));
        assertEquals("dog dog", p.getText());
        history.undo();
        assertEquals("cat cat", p.getText());
        assertFalse(history.canUndo());
        history.redo();
        assertEquals("dog dog", p.getText());
    }

    @Test
    void failingStepUndoesEarlierStepsAndRethrows() {
        Paragraph p = new Paragraph("abc", PLAIN);
        CompositeCommand bad = new CompositeCommand(List.of(
                new InsertTextCommand(p, 0, "x"),
                new InsertTextCommand(p, 0, "y"),
                new InsertTextCommand(p, 99, "z")));
        assertThrows(IndexOutOfBoundsException.class, bad::execute);
        assertEquals("abc", p.getText());
    }

    @Test
    void failingCompositeIsNotRecordedInHistory() {
        Document doc = new Document();
        Paragraph p = new Paragraph("abc", PLAIN);
        doc.add(p);
        CommandHistory history = new CommandHistory(doc);
        assertThrows(IndexOutOfBoundsException.class, () -> history.execute(new CompositeCommand(
                List.of(new InsertTextCommand(p, 0, "x"), new DeleteTextCommand(p, 0, 50)))));
        assertFalse(history.canUndo());
        assertEquals("abc", p.getText());
    }

    @Test
    void emptyCompositeDoesNothing() {
        CompositeCommand empty = new CompositeCommand(List.of());
        empty.execute();
        empty.undo();
        assertTrue(empty.getSteps().isEmpty());
    }

    @Test
    void nullStepsRejected() {
        assertThrows(NullPointerException.class, () -> new CompositeCommand(null));
        java.util.ArrayList<EditCommand> withNull = new java.util.ArrayList<>();
        withNull.add(null);
        assertThrows(NullPointerException.class, () -> new CompositeCommand(withNull));
    }

    @Test
    void nestedCompositesWork() {
        Paragraph p = new Paragraph("", PLAIN);
        CompositeCommand inner = new CompositeCommand(List.of(new InsertTextCommand(p, 0, "b")));
        CompositeCommand outer = new CompositeCommand(List.of(inner, new InsertTextCommand(p, 0, "a")));
        outer.execute();
        assertEquals("ab", p.getText());
        outer.undo();
        assertEquals("", p.getText());
    }
}
