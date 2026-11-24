
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

    @Test
    void testNegativePosition() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("insert 0 -5 \"Hello\""));
    }

    @Test
    void testInvalidWidth() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("width 0"));
    }

    @Test
    void testInvalidReplaceArguments() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse(
                        "replace \"Java\" \"Python\" wrong"));
    }

    @Test
    void testInvalidDeleteRange() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("delete 0 10 5"));
    }

    @Test
    void testValidInsertCommand() {
        var command =
                parser.parse("insert 0 5 \"Hello world\"");

        assertEquals("insert", command.name());
        assertEquals(3, command.arguments().size());
        assertEquals("Hello world",
                command.arguments().get(2));
    }

    @Test
    void testInvalidBreaker() {
        assertThrows(IllegalArgumentException.class,
                () -> parser.parse("breaker unknown"));
    }
}
