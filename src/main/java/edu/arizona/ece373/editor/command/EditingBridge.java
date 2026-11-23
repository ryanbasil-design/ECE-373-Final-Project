
package edu.arizona.ece373.editor.command;

/**
 * Connects the command layer to the editing and history system.
 */
public interface EditingBridge {

    // Executes an editing command
    void execute(CommandParser.ParsedCommand command);

    // Undoes the previous change
    void undo();

    // Redoes an undone change
    void redo();
}
