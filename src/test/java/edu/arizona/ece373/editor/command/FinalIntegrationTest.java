
package edu.arizona.ece373.editor.command;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

/**
 * End-to-end checks for the final command integrations.
 */
public class FinalIntegrationTest {

    // Runs a script and returns the console output
    private String run(String script) throws Exception {

        EditorApp app = new EditorApp();

        ByteArrayOutputStream bytes =
                new ByteArrayOutputStream();

        try (PrintStream out =
                     new PrintStream(bytes)) {

            app.run(
                    new StringReader(script), out);
        }

        return bytes.toString();
    }

    @Test
    void createsNestedSectionsAndParagraphs()
            throws Exception {

        String output = run(
                "section \"Chapter\"\n"
                        + "section \"Part\" inside 0\n"
                        + "paragraph \"Nested text\" inside 1\n"
                        + "view plain\n");

        assertTrue(output.contains("Chapter"));
        assertTrue(output.contains("Part"));
        assertTrue(output.contains("Nested text"));
        assertFalse(output.contains("Error:"));
    }

    @Test
    void formatsSectionTitleAndUndoesIt()
            throws Exception {

        String output = run(
                "section \"Chapter\"\n"
                        + "format title 0 0 7 bold\n"
                        + "view markup\n"
                        + "undo\n"
                        + "view markup\n");

        assertTrue(
                output.contains("[bold]Chapter[/]"));

        assertTrue(
                output.contains("Undo completed."));

        assertFalse(output.contains("Error:"));
    }

    @Test
    void macroCanCreateThenEditParagraphWithOneUndo()
            throws Exception {

        String output = run(
                "paragraph \"Seed\"\n"
                        + "macro start add\n"
                        + "paragraph \"Hi\"\n"
                        + "insert 1 2 \"!\"\n"
                        + "macro stop\n"
                        + "undo\n"
                        + "undo\n"
                        + "macro replay add\n"
                        + "view plain\n"
                        + "undo\n"
                        + "view plain\n"
                        + "redo\n"
                        + "view plain\n");

        assertTrue(
                output.contains("Macro replayed: add"));

        assertTrue(output.contains("Hi!"));

        assertTrue(
                output.contains("Redo completed."));

        assertFalse(output.contains("Error:"));
    }

    @Test
    void mixedMacroReplaysDisplayAndEditing()
            throws Exception {

        String output = run(
                "paragraph \"Seed\"\n"
                        + "macro start mixed\n"
                        + "insert 0 4 \"!\"\n"
                        + "view plain\n"
                        + "macro stop\n"
                        + "undo\n"
                        + "macro replay mixed\n"
                        + "undo\n"
                        + "view plain\n");

        assertTrue(output.contains("Seed!"));

        assertTrue(
                output.contains("Macro replayed: mixed"));

        assertTrue(
                output.contains("Undo completed."));

        assertFalse(output.contains("Error:"));
    }

    @Test
    void textMoveWorksWithUndo()
            throws Exception {

        String output = run(
                "paragraph \"abcdef\"\n"
                        + "move 0 1 3 0 6\n"
                        + "view plain\n"
                        + "undo\n"
                        + "view plain\n");

        assertTrue(output.contains("adefbc"));
        assertTrue(output.contains("abcdef"));
        assertFalse(output.contains("Error:"));
    }
}
