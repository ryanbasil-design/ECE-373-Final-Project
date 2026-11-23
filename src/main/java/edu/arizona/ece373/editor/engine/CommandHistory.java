package edu.arizona.ece373.editor.engine;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Objects;

/**
 * Runs editing commands and keeps the undo and redo stacks. After every
 * change it notifies the document's observers so things like the statistics
 * stay current.
 */
public class CommandHistory {

    private final Document document;
    private final Deque<EditCommand> undoStack = new ArrayDeque<>();
    private final Deque<EditCommand> redoStack = new ArrayDeque<>();

    /**
     * Creates an empty history for the given document.
     *
     * @param document the document whose observers are notified after changes
     * @throws NullPointerException if document is null
     */
    public CommandHistory(Document document) {
        this.document = Objects.requireNonNull(document, "document must not be null");
    }

    /**
     * Executes a command and adds it to the undo stack. Any redo history is
     * cleared, since a new change starts a new branch of history. If the
     * command throws, nothing is recorded.
     *
     * @param command the command to run
     * @throws NullPointerException if command is null
     */
    public void execute(EditCommand command) {
        Objects.requireNonNull(command, "command must not be null");
        command.execute();
        undoStack.push(command);
        redoStack.clear();
        document.notifyObservers();
    }

    /**
     * Undoes the most recent command and moves it to the redo stack.
     *
     * @throws IllegalStateException if there is nothing to undo
     */
    public void undo() {
        if (undoStack.isEmpty()) {
            throw new IllegalStateException("Nothing to undo");
        }
        EditCommand command = undoStack.pop();
        command.undo();
        redoStack.push(command);
        document.notifyObservers();
    }

    /**
     * Redoes the most recently undone command and moves it back to the undo
     * stack.
     *
     * @throws IllegalStateException if there is nothing to redo
     */
    public void redo() {
        if (redoStack.isEmpty()) {
            throw new IllegalStateException("Nothing to redo");
        }
        EditCommand command = redoStack.pop();
        command.execute();
        undoStack.push(command);
        document.notifyObservers();
    }

    /**
     * Returns whether there is a command that can be undone.
     *
     * @return true if undo() would succeed
     */
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Returns whether there is a command that can be redone.
     *
     * @return true if redo() would succeed
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
