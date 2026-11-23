
package edu.arizona.ece373.editor.command;

import java.util.List;

/**
 * Connects the command layer to the editing and history system.
 */
public interface EditingBridge {

    // Executes one editing command
    void execute(CommandParser.ParsedCommand command);

    // Executes multiple commands as one undoable operation
    default void executeBatch(
            List<CommandParser.ParsedCommand> commands) {

        throw new IllegalStateException(
                "Batch editing is not connected yet");
    }

    // Executes planned text replacements
    default void executeReplacements(
            List<SearchEngine.Replacement> replacements) {

        throw new IllegalStateException(
                "Replacement editing is not connected yet");
    }

    // Undoes the previous change
    void undo();

    // Redoes an undone change
    void redo();
}
