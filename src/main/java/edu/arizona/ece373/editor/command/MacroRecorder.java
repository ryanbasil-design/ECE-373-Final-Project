
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

    /**
     * makes a macro recorder with no macros
     */
    public MacroRecorder() {
        macros = new HashMap<>();
        currentCommands = new ArrayList<>();
        currentName = null;
    }

    /**
     * starts recording a new macro
     *
     * @param name the macro name
     */
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

    /**
     * adds a command to the macro being recorded
     *
     * @param command the command line to save
     */
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

    /**
     * stops recording and saves the macro
     */
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

    /**
     * gets the commands saved in a macro
     *
     * @param name the macro name
     * @return the saved commands in order
     */
    public List<String> getCommands(String name) {
        if (!macros.containsKey(name)) {
            throw new IllegalArgumentException(
                    "Macro does not exist: " + name);
        }

        return List.copyOf(macros.get(name));
    }

    /**
     * checks if a macro with this name exists
     *
     * @param name the macro name
     * @return true if it exists
     */
    public boolean hasMacro(String name) {
        return macros.containsKey(name);
    }

    /**
     * checks if a macro is being recorded right now
     *
     * @return true if recording
     */
    public boolean isRecording() {
        return currentName != null;
    }

    /**
     * gets how many macros are saved
     *
     * @return the number of macros
     */
    public int getMacroCount() {
        return macros.size();
    }
}
