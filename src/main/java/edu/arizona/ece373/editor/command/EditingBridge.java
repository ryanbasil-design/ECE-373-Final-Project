
package edu.arizona.ece373.editor.command;

import java.util.List;

/**
 * Connects the command layer to the editing and history system.
 */
public interface EditingBridge {

    /**
     * runs one editing command
     *
     * @param command the parsed command
     */
    void execute(CommandParser.ParsedCommand command);

    /**
     * runs several commands as one undoable change
     *
     * @param commands the parsed commands in order
     */
    default void executeBatch(
            List<CommandParser.ParsedCommand> commands) {

        throw new IllegalStateException(
                "Batch editing is not connected yet");
    }

    /**
     * runs planned text replacements as one undoable change
     *
     * @param replacements the replacements to make
     */
    default void executeReplacements(
            List<SearchEngine.Replacement> replacements) {

        throw new IllegalStateException(
                "Replacement editing is not connected yet");
    }

    /**
     * undoes the last change
     */
    void undo();

    /**
     * redoes the last undone change
     */
    void redo();
}
