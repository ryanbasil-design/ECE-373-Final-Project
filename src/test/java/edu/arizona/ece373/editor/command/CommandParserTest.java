
package edu.arizona.ece373.editor.command;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class CommandParserTest {

    private final CommandParser parser = new CommandParser();

    @Test
    void testUndoCommand() {
        CommandParser.ParsedCommand command = parser.parse("undo");

        assertEquals("undo", command.name());
        assertTrue(command.arguments().isEmpty());
    }

    @Test
    void testCheckpointCommand() {
        CommandParser.ParsedCommand command =
                parser.parse("checkpoint save firstDraft");

        assertEquals("checkpoint", command.name());
        assertEquals("save", command.arguments().get(0));
        assertEquals("firstDraft", command.arguments().get(1));
    }

    @Test
    void testQuotedText() {
        CommandParser.ParsedCommand command =
                parser.parse("find \"hello world\" whole");

        assertEquals("hello world", command.arguments().get(0));
        assertEquals("whole", command.arguments().get(1));
    }

    @Test
    void testInvalidCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("somethinginvalid"));
    }

    @Test
    void testEmptyCommand() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(""));
    }

    @Test
    void testMissingQuotes() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("find \"hello world whole"));
    }

    @Test
    void testInvalidCheckpoint() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("checkpoint delete firstDraft"));
    }

    @Test
    void testMacroRecording() {
        CommandParser.ParsedCommand command =
                parser.parse("macro start formatting");

        assertEquals("macro", command.name());
        assertEquals("start", command.arguments().get(0));
        assertEquals("formatting", command.arguments().get(1));
    }
}
