package edu.arizona.ece373.editor.engine;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * A titled part of the document that holds an ordered list of child
 * elements, which can be paragraphs or nested sections to any depth.
 * This is the composite role of the Composite pattern.
 * The title is stored as a Paragraph so it supports the same character
 * formatting as paragraph text. Title words and characters are included
 * in word and character counts, but the title is not counted as a paragraph.
 */

public class Section extends DocumentElement {

    private final Paragraph title;
    private final List<DocumentElement> children = new ArrayList<>();

    /**
     * Creates an empty section with the given title.
     *
     * @param title the section title
     * @throws NullPointerException if title is null
     */
    public Section(Paragraph title) {
        this.title = Objects.requireNonNull(title, "title must not be null");
    }

    /**
     * Creates an empty section whose title is the given text in one format.
     *
     * @param titleText the title text
     * @param format    the format applied to the title
     */
    public Section(String titleText, CharacterFormat format) {
        this(new Paragraph(titleText, format));
    }

    /**
     * Returns the title of this seciton. Editing the returned paragraph
     * edits the title
     *
     * @return  the title paragraph
     */
    public Paragraph getTitle() {
        return title;
    }

    /**
     * Returns a read-only copy of this section's children, in order.
     *
     * @return  the child elements
     */
    public List<DocumentElement> getChildren() {
        return List.copyOf(children);
    }

    /**
     * Adds a child element on the end of this section.
     *
     * @param child the paragraph or section to add
     * @throws NullPointerException if child is null
     * @throws IllegalArgumentException if adding child would place a section inside itself
     */
    public void add(DocumentElement child) {
        add(children.size(), child);
    }

    /**
     * Inserts a child element at the given index, shifting later children back.
     *
     * @param index the position to insert at, from 0 to the number of children
     * @param child the paragraph or section to add
     * @throws IndexOutOfBoundsException if index is outside that range
     * @throws NullPointerException if child is null
     * @throws IllegalArgumentException if adding child would place a section inside itself
     */
    public void add(int index, DocumentElement child) {
        Objects.requireNonNull(child, "child must not be null");
        if (index < 0 || index > children.size()) {
            throw new IndexOutOfBoundsException(
                    "index " + index + " is outside the section (size " + children.size() + ")");
        }
        if (child instanceof Section s && (s == this || s.contains(this))) {
            throw new IllegalArgumentException("a section cannot be placed inside itself");
        }
        children.add(index, child);
    }

    /**
     * Removes and returns the child at the given index.
     *
     * @param index the position of the child to remove
     * @return      the removed child
     * @throws IndexOutOfBoundsException if no child exists at that index
     */
    public DocumentElement remove(int index) {
        if (index < 0 || index >= children.size()) {
            throw new IndexOutOfBoundsException(
                    "index " + index + " is outside the section (size " + children.size() + ")");
        }
        return children.remove(index);
    }

    /**
     * Returns the index of the given child in this section.
     *
     * @param child the element to look for
     * @return      its index, or -1 if it is not a direct child of this section
     */
    public int indexOf(DocumentElement child) {
        return children.indexOf(child);
    }

    /**
     * Returns the title followed by each child's text, separated by newlines.
     *
     * @return the text of this section
     */
    @Override
    public String getText() {
        StringBuilder sb = new StringBuilder(title.getText());
        for (DocumentElement child : children) {
            sb.append('\n').append(child.getText());
        }
        return sb.toString();
    }

    /**
     * Returns the number of words in the title and all children.
     *
     * @return the word count
     */
    @Override
    public int countWords() {
        int total = title.countWords();
        for (DocumentElement child : children) {
            total += child.countWords();
        }
        return total;
    }

    /**
     * Returns the number of characters in the title and all children,
     * including spaces. Line separators added by getText are not counted.
     *
     * @return the character count
     */
    @Override
    public int countChars() {
        int total = title.countChars();
        for (DocumentElement child : children) {
            total += child.countChars();
        }
        return total;
    }

    /**
     * Returns the number of paragraphs in this section's children at any
     * depth. The title is not counted as a paragraph.
     *
     * @return the paragraph count
     */
    @Override
    public int countParagraphs() {
        int total = 0;
        for (DocumentElement child : children) {
            total += child.countParagraphs();
        }
        return total;
    }

    /** Returns true if the given element is anywhere inside this section. */
    private boolean contains(DocumentElement target) {
        for (DocumentElement child : children) {
            if (child == target) {
                return true;
            }
            if (child instanceof Section s && s.contains(target)) {
                return true;
            }
        }
        return false;
    }
}
