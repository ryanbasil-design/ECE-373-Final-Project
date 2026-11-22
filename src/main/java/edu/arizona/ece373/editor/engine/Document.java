package edu.arizona.ece373.editor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * The root of the document tree. Holds the top-level sections and
 * paragraphs in order and notifies registered observers when the
 * document changes. This is the subject role of the Observer pattern.
 * Content is stored in a private, untitled root Section, so the document
 * reuses Section's ordering, validation, and recursive counting without
 * being a Section itself.
 * Editing operations do not notify observers directly. CommandHistory calls
 * notifyObservers after each execute, undo, and redo.
 */
public class Document {

    private final Section root = new Section("", CharacterFormat.DEFAULT);
    private final List<DocumentObserver> observers = new ArrayList<>();

    /**
     * Returns a read-only copy of the top-level elements, in order.
     *
     * @return the top-level sections and paragraphs
     */
    public List<DocumentElement> getElements() {
        return root.getChildren();
    }

    /**
     * Adds a section or paragraph to the end of the document.
     *
     * @param element the element to add
     * @throws NullPointerException if element is null
     */
    public void add(DocumentElement element) {
        root.add(element);
    }

    /**
     * Inserts a section or paragraph at the given top-level index.
     *
     * @param index   the position to insert at, from 0 to the number of top-level elements
     * @param element the element to add
     * @throws IndexOutOfBoundsException if index is outside that range
     * @throws NullPointerException      if element is null
     */
    public void add(int index, DocumentElement element) {
        root.add(index, element);
    }

    /**
     * Removes and returns the top-level element at the given index.
     *
     * @param index the position of the element to remove
     * @return the removed element
     * @throws IndexOutOfBoundsException if no element exists at that index
     */
    public DocumentElement remove(int index) {
        return root.remove(index);
    }

    /**
     * Returns the top-level index of the given element.
     *
     * @param element the element to look for
     * @return its index, or -1 if it is not a top-level element
     */
    public int indexOf(DocumentElement element) {
        return root.indexOf(element);
    }

    /**
     * Returns the text of every top-level element, separated by newlines.
     *
     * @return the document text
     */
    public String getText() {
        StringBuilder sb = new StringBuilder();
        for (DocumentElement element : root.getChildren()) {
            if (!sb.isEmpty()) {
                sb.append('\n');
            }
            sb.append(element.getText());
        }
        return sb.toString();
    }

    /**
     * Returns the number of words in the document, including section titles.
     *
     * @return the word count
     */
    public int countWords() {
        return root.countWords();
    }

    /**
     * Returns the number of characters in the document, including spaces
     * and section titles.
     *
     * @return the character count
     */
    public int countChars() {
        return root.countChars();
    }

    /**
     * Returns the number of paragraphs in the document. Section titles
     * are not counted as paragraphs.
     *
     * @return the paragraph count
     */
    public int countParagraphs() {
        return root.countParagraphs();
    }

    /**
     * Registers an observer to be notified when the document changes.
     *
     * @param observer the observer to add
     * @throws NullPointerException if observer is null
     */
    public void addObserver(DocumentObserver observer) {
        observers.add(Objects.requireNonNull(observer, "observer must not be null"));
    }

    /**
     * Stops notifying the given observer. Does nothing if it was not registered.
     *
     * @param observer the observer to remove
     */
    public void removeObserver(DocumentObserver observer) {
        observers.remove(observer);
    }

    /**
     * Notifies every registered observer that the document has changed.
     * Called by CommandHistory after each execute, undo, and redo.
     */
    public void notifyObservers() {
        for (DocumentObserver observer : List.copyOf(observers)) {
            observer.onDocumentChanged(this);
        }
    }
}
