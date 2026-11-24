
package edu.arizona.ece373.editor.command;

import edu.arizona.ece373.editor.engine.CharacterFormat;
import edu.arizona.ece373.editor.engine.Paragraph;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class EditorAppTest {

    // Runs commands and returns the output
    private String runCommands(String commands) throws Exception {
        EditorApp app = new EditorApp();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(
                bytes, true, StandardCharsets.UTF_8)) {

            app.run(new StringReader(commands), output);
        }

        return bytes.toString(StandardCharsets.UTF_8);
    }

    @Test
    void testHelp() throws Exception {
        String result = runCommands("help\nexit\n");

        assertTrue(result.contains("Available commands"));
        assertTrue(result.contains("Goodbye!"));
    }

    @Test
    void testStatistics() throws Exception {
        String result = runCommands("stats\nexit\n");

        assertTrue(result.contains("Words: 0"));
        assertTrue(result.contains("Characters: 0"));
        assertTrue(result.contains("Paragraphs: 0"));
    }

    @Test
    void testCheckpointSaveAndRestore() throws Exception {
        String result = runCommands(
                "checkpoint save first\n"
                        + "checkpoint restore first\n"
                        + "exit\n");

        assertTrue(result.contains("Checkpoint saved: first"));
        assertTrue(result.contains("Checkpoint restored: first"));
    }

    @Test
    void testMissingCheckpoint() throws Exception {
        String result = runCommands(
                "checkpoint restore missing\nexit\n");

        assertTrue(result.contains("Error:"));
        assertTrue(result.contains("Checkpoint does not exist"));
    }

    @Test
    void testMacroRecording() throws Exception {
        String result = runCommands(
                "macro start example\n"
                        + "stats\n"
                        + "macro stop\n"
                        + "exit\n");

        assertTrue(result.contains("Recording started."));
        assertTrue(result.contains("Recording saved."));
    }

    @Test
    void testInvalidCommand() throws Exception {
        String result = runCommands(
                "invalidcommand\nstats\nexit\n");

        assertTrue(result.contains("Error:"));
        assertTrue(result.contains("Words: 0"));
    }

    @Test
    void testFindEmptyDocument() throws Exception {
        String result = runCommands(
                "find \"Java\" whole\nexit\n");

        assertTrue(result.contains("Matches found: 0"));
    }

    @Test
    void testExitStopsExecution() throws Exception {
        String result = runCommands(
                "exit\nstats\n");

        assertTrue(result.contains("Goodbye!"));
        assertFalse(result.contains("Words:"));
    }

    @Test
    void testMultipleCommands() throws Exception {
        String result = runCommands(
                "checkpoint save first\n"
                        + "stats\n"
                        + "checkpoint restore first\n"
                        + "stats\n"
                        + "exit\n");

        assertTrue(result.contains("Checkpoint saved: first"));
        assertTrue(result.contains("Checkpoint restored: first"));

        assertEquals(2,
                result.split("Words: 0", -1).length - 1);
    }

    @Test
    void testInvalidCommandDoesNotStopScript() throws Exception {
        String result = runCommands(
                "invalidcommand\n"
                        + "checkpoint save backup\n"
                        + "stats\n"
                        + "exit\n");

        assertTrue(result.contains("Error:"));
        assertTrue(result.contains("Checkpoint saved: backup"));
        assertTrue(result.contains("Words: 0"));
        assertTrue(result.contains("Goodbye!"));
    }

    @Test
    void testSameCommandsProduceSameOutput() throws Exception {
        String commands =
                "checkpoint save draft\n"
                        + "stats\n"
                        + "checkpoint restore draft\n"
                        + "exit\n";

        String firstRun = runCommands(commands);
        String secondRun = runCommands(commands);

        assertEquals(firstRun, secondRun);
    }

    @Test
    void testPresentationIntegration() throws Exception {
        EditorApp app = new EditorApp();

        app.getDocument().add(new Paragraph(
                "Hello world from ECE 373",
                CharacterFormat.DEFAULT));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(
                bytes, true, StandardCharsets.UTF_8)) {

            app.run(new StringReader(
                    "view plain\n"
                            + "width 10\n"
                            + "breaker greedy\n"
                            + "view\n"
                            + "exit\n"), output);
        }

        String result = bytes.toString(StandardCharsets.UTF_8);

        assertTrue(result.contains("Hello world from ECE 373"));
        assertTrue(result.contains("Hello\nworld from\nECE 373"));
    }

    // Fake editing system used to test the connection
    private static class FakeEditingBridge implements EditingBridge {

        String lastCommand;
        int undoCount = 0;
        int redoCount = 0;
        int batchCount = 0;

        int replacementCount = 0;

        @Override
        public void executeReplacements(
                List<SearchEngine.Replacement> replacements) {

            replacementCount = replacements.size();
        }


        @Override
        public void execute(CommandParser.ParsedCommand command) {
            lastCommand = command.name();
        }

        @Override
        public void undo() {
            undoCount++;
        }

        @Override
        public void redo() {
            redoCount++;
        }

        // Records when a group of commands is received
        @Override
        public void executeBatch(
                List<CommandParser.ParsedCommand> commands) {

            batchCount++;
        }
    }

    @Test
    void testEditingBridgeReceivesCommands() throws Exception {
        EditorApp app = new EditorApp();
        FakeEditingBridge bridge = new FakeEditingBridge();

        app.setEditingBridge(bridge);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "insert 0 5 \"Hello\"\n"), output);
        }

        assertEquals("insert", bridge.lastCommand);
    }

    @Test
    void testUndoAndRedoUseBridge() throws Exception {
        EditorApp app = new EditorApp();
        FakeEditingBridge bridge = new FakeEditingBridge();

        app.setEditingBridge(bridge);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "undo\nredo\n"), output);
        }

        assertEquals(1, bridge.undoCount);
        assertEquals(1, bridge.redoCount);
    }


    @Test
    void testEditingBridgeConnectedAutomatically() throws Exception {
        EditorApp app = new EditorApp();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "paragraph \"Hello\"\n"
                            + "insert 0 5 \" world\"\n"
                            + "view plain\n"
                            + "undo\n"
                            + "view plain\n"), output);
        }

        String result = bytes.toString();

        // The real editing system should work automatically
        assertTrue(result.contains("Hello world"));
        assertTrue(result.contains("Undo completed."));

        // The editor should not show the old connection error
        assertFalse(result.contains(
                "Editing system is not connected yet"));
    }


    @Test
    void testCheckpointRestoreUpdatesViewAndStats() throws Exception {
        EditorApp app = new EditorApp();

        // Create original document
        app.getDocument().add(new Paragraph(
                "Hello world", CharacterFormat.DEFAULT));

        app.getDocument().notifyObservers();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {

            app.run(new StringReader(
                    "checkpoint save original\n"), output);

            // Add text after saving the checkpoint
            app.getDocument().add(new Paragraph(
                    "Extra text", CharacterFormat.DEFAULT));

            app.getDocument().notifyObservers();

            // Restore the saved document
            app.run(new StringReader(
                    "checkpoint restore original\n"
                            + "view\n"
                            + "stats\n"
                            + "exit\n"), output);
        }

        String result = bytes.toString();

        assertTrue(result.contains("Checkpoint restored: original"));
        assertTrue(result.contains("Hello world"));
        assertFalse(result.contains("Extra text"));
        assertTrue(result.contains("Words: 2"));
        assertTrue(result.contains("Paragraphs: 1"));
    }

    // Tests the new batch editing connection
    @Test
    void testEditingBridgeBatch() {
        FakeEditingBridge bridge = new FakeEditingBridge();

        CommandParser parser = new CommandParser();

        var first = parser.parse("insert 0 0 \"Hello\"");
        var second = parser.parse("insert 0 5 \" world\"");

        bridge.executeBatch(List.of(first, second));

        assertEquals(1, bridge.batchCount);
    }

    @Test
    void testMacroReplayUsesBatch() throws Exception {
        EditorApp app = new EditorApp();
        FakeEditingBridge bridge = new FakeEditingBridge();

        app.setEditingBridge(bridge);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "macro start editing\n"
                            + "insert 0 0 \"Hello\"\n"
                            + "insert 0 5 \" world\"\n"
                            + "macro stop\n"
                            + "macro replay editing\n"
                            + "exit\n"), output);
        }

        // Two commands execute during recording
        assertEquals("insert", bridge.lastCommand);

        // Replay sends one batch
        assertEquals(1, bridge.batchCount);

        assertTrue(bytes.toString().contains(
                "Macro replayed: editing"));
    }

    @Test
    void testMacroReplayWithoutBridge() throws Exception {
        EditorApp app = new EditorApp();

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "macro start test\n"
                            + "macro stop\n"
                            + "macro replay test\n"
                            + "exit\n"), output);
        }

        // Empty macros can replay without editing
        assertTrue(bytes.toString().contains(
                "Macro replayed: test"));
    }

    @Test
    void testReplaceAllUsesBridge() throws Exception {
        EditorApp app = new EditorApp();
        FakeEditingBridge bridge = new FakeEditingBridge();
        app.setEditingBridge(bridge);

        app.getDocument().add(new Paragraph(
                "Java and Java", CharacterFormat.DEFAULT));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "replaceall \"Java\" \"Python\" whole\n"), output);
        }

        assertEquals(2, bridge.replacementCount);
        assertTrue(bytes.toString().contains(
                "Replacements completed: 2"));
    }

    @Test
    void testReplaceFirstUsesBridge() throws Exception {
        EditorApp app = new EditorApp();
        FakeEditingBridge bridge = new FakeEditingBridge();
        app.setEditingBridge(bridge);

        app.getDocument().add(new Paragraph(
                "Java and Java", CharacterFormat.DEFAULT));

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();

        try (PrintStream output = new PrintStream(bytes)) {
            app.run(new StringReader(
                    "replace \"Java\" \"Python\" whole\n"), output);
        }

        assertEquals(1, bridge.replacementCount);
        assertTrue(bytes.toString().contains(
                "Replacements completed: 1"));
    }

}
