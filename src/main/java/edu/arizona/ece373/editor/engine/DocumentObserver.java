package edu.arizona.ece373.editor.engine;

/**
 * Receives a notification whenever the document changes.
 * Components such as statistics, a change log, or auto-save implemnt this
 * interface and register with the Document, so new reactions to document
 * changes can be added without changing any editing operations.
 * This is the observer role of the Observer pattern.
 */
public interface DocumentObserver {

    /**
     * Called after the document has changed.
     *
     * @param document the document that changed
     */
    void onDocumentChanged(Document document);
}
