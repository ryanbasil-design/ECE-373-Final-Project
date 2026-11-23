package edu.arizona.ece373.editor.engine;

/**
 * One change to the document that can be done and undone. Every editing
 * operation is its own EditCommand so CommandHistory can keep it on the undo
 * and redo stacks (the Command pattern).
 */
public interface EditCommand {

    /**
     * Applies this change to the document. If the change is not valid, an
     * exception is thrown and the document is left unchanged.
     */
    void execute();

    /**
     * Reverses this change, putting the document back the way it was right
     * before execute() was called.
     */
    void undo();
}
