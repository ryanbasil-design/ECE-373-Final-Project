
package edu.arizona.ece373.editor.command;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Records and stores sequences of editor commands.
 */
public class MacroRecorder {

    private final Map<String, List<String>> macros;
    private List<String> currentCommands;
    private String currentName;

    public MacroRecorder() {
        macros = new HashMap<>();
        currentCommands = new ArrayList<>();
        currentName = null;
    }

    // Starts recording a new macro
    public void startRecording(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException(
                    "Macro name cannot be empty");
        }

        if (isRecording()) {
            throw new IllegalStateException(
                    "Already recording a macro");
        }

        currentName = name;
        currentCommands = new ArrayList<>();
    }

    // Adds a command to the current recording
    public void recordCommand(String command) {
        if (!isRecording()) {
            return;
        }

        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException(
                    "Command cannot be empty");
        }

        currentCommands.add(command);
    }

    // Stops recording and saves the macro
    public void stopRecording() {
        if (!isRecording()) {
            throw new IllegalStateException(
                    "No macro is being recorded");
        }

        macros.put(currentName,
                new ArrayList<>(currentCommands));

        currentName = null;
        currentCommands = new ArrayList<>();
    }

    // Returns the commands saved in a macro
    public List<String> getCommands(String name) {
        if (!macros.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Macro does not exist: " + name);
        }

        return List.copyOf(macros.get(name));
    }

    // Checks whether a macro exists
    public boolean hasMacro(String name) {
        return macros.containsKey(name);
    }

    // Checks if recording is active
    public boolean isRecording() {
        return currentName != null;
    }

    // Returns the number of saved macros
    public int getMacroCount() {
        return macros.size();
    }
}
