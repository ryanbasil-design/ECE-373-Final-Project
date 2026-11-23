
package edu.arizona.ece373.editor.command;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class MacroRecorderTest {

    @Test
    void testStartAndStopRecording() {
        MacroRecorder recorder = new MacroRecorder();

        assertFalse(recorder.isRecording());

        recorder.startRecording("test");
        assertTrue(recorder.isRecording());

        recorder.stopRecording();
        assertFalse(recorder.isRecording());
        assertTrue(recorder.hasMacro("test"));
    }

    @Test
    void testRecordCommands() {
        MacroRecorder recorder = new MacroRecorder();

        recorder.startRecording("formatting");
        recorder.recordCommand("bold 0 5");
        recorder.recordCommand("size 0 5 16");
        recorder.stopRecording();

        List<String> commands =
                recorder.getCommands("formatting");

        assertEquals(2, commands.size());
        assertEquals("bold 0 5", commands.get(0));
        assertEquals("size 0 5 16", commands.get(1));
    }

    @Test
    void testMissingMacro() {
        MacroRecorder recorder = new MacroRecorder();

        assertThrows(IllegalArgumentException.class,
                () -> recorder.getCommands("missing"));
    }

    @Test
    void testInvalidMacroName() {
        MacroRecorder recorder = new MacroRecorder();

        assertThrows(IllegalArgumentException.class,
                () -> recorder.startRecording(""));
    }

    @Test
    void testCannotRecordTwoAtOnce() {
        MacroRecorder recorder = new MacroRecorder();

        recorder.startRecording("first");

        assertThrows(IllegalStateException.class,
                () -> recorder.startRecording("second"));
    }

    @Test
    void testMultipleMacros() {
        MacroRecorder recorder = new MacroRecorder();

        recorder.startRecording("first");
        recorder.recordCommand("insert hello");
        recorder.stopRecording();

        recorder.startRecording("second");
        recorder.recordCommand("delete 0 5");
        recorder.stopRecording();

        assertEquals(2, recorder.getMacroCount());
        assertEquals(1, recorder.getCommands("first").size());
        assertEquals(1, recorder.getCommands("second").size());
    }
}
