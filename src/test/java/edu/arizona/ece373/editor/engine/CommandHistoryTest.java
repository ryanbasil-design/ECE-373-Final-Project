package edu.arizona.ece373.editor.engine;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static edu.arizona.ece373.editor.engine.EditTestFormats.PLAIN;
import static org.junit.jupiter.api.Assertions.*;

class CommandHistoryTest {

    private Document document;
    private Paragraph paragraph;
    private CommandHistory history;
    private int notifications;

    @BeforeEach
    void setUp() {
        document = new Document();
        paragraph = new Paragraph("Hello", PLAIN);
        document.add(paragraph);
        history = new CommandHistory(document);
        notifications = 0;
        document.addObserver(d -> notifications++);
    }

    @Test
    void executeRunsTheCommandAndAllowsUndo() {
        history.execute(new InsertTextCommand(paragraph, 5, " world"));
        assertEquals("Hello world", paragraph.getText());
        assertTrue(history.canUndo());
        assertFalse(history.canRedo());
    }

    @Test
    void undoAndRedoMoveBetweenStacks() {
        history.execute(new InsertTextCommand(paragraph, 5, "!"));
        history.undo();
        assertEquals("Hello", paragraph.getText());
        assertTrue(history.canRedo());
        history.redo();
        assertEquals("Hello!", paragraph.getText());
        assertFalse(history.canRedo());
    }

    @Test
    void historyHasNoSmallFixedLimit() {
        for (int i = 0; i < 1000; i++) {
            history.execute(new InsertTextCommand(paragraph, 0, "x"));
        }
        for (int i = 0; i < 1000; i++) {
            history.undo();
        }
        assertEquals("Hello", paragraph.getText());
    }

    @Test
    void newEditClearsRedoHistory() {
        history.execute(new InsertTextCommand(paragraph, 5, "!"));
        history.undo();
        history.execute(new InsertTextCommand(paragraph, 0, ">"));
        assertFalse(history.canRedo());
        assertThrows(IllegalStateException.class, history::redo);
    }

    @Test
    void undoWithNothingToUndoThrows() {
        IllegalStateException e = assertThrows(IllegalStateException.class, history::undo);
        assertEquals("Nothing to undo", e.getMessage());
    }

    @Test
    void redoWithNothingToRedoThrows() {
        assertThrows(IllegalStateException.class, history::redo);
    }

    @Test
    void failedCommandIsNotRecordedAndChangesNothing() {
        assertThrows(IndexOutOfBoundsException.class,
                () -> history.execute(new InsertTextCommand(paragraph, 99, "x")));
        assertEquals("Hello", paragraph.getText());
        assertFalse(history.canUndo());
        assertEquals(0, notifications);
    }

    @Test
    void observersAreNotifiedOnExecuteUndoAndRedo() {
        history.execute(new InsertTextCommand(paragraph, 0, "a"));
        history.undo();
        history.redo();
        assertEquals(3, notifications);
    }

    @Test
    void nullArgumentsAreRejected() {
        assertThrows(NullPointerException.class, () -> new CommandHistory(null));
        assertThrows(NullPointerException.class, () -> history.execute(null));
    }
}
