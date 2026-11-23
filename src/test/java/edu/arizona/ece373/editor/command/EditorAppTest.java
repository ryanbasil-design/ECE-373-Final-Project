
package edu.arizona.ece373.editor.command;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

public class EditorAppTest {

    // Runs commands and returns the console output
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
}
